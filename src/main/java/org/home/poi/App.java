package org.home.poi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String inputFile = args[0];
        String outFile = args[1];
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(App.class.getResourceAsStream("/root.xml"));
        List<Poi> poiList = parseJson(inputFile);
        Document wptDoc = db.parse(App.class.getResourceAsStream("/wpt.xml"));
        Element wpt = (Element) doc.importNode(wptDoc.getDocumentElement(), true);
        for (Poi poi: poiList) {
            Element documentElement = (Element) wpt.cloneNode(true);
            documentElement.setAttribute("lat", poi.getLatitude());
            documentElement.setAttribute("lon", poi.getLongitude());
            documentElement.getElementsByTagName("desc").item(0).setTextContent(poi.getDescription());
            doc.getDocumentElement().appendChild(documentElement);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outFile));
        transformer.transform(source, result);

    }

    private static List<Poi> parseJson(String inputFile) throws IOException {
        List<Map<String, String>> jsonList = new ObjectMapper().readValue(new FileInputStream(new File(inputFile)), new TypeReference<List<Map<String, String>>>() {});
        return jsonList.stream().map(Poi::new).collect(Collectors.toList());
    }
}
