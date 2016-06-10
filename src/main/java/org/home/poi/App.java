package org.home.poi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
public class App {
    //private static String[] counties = new String[]{"Austria","Belgium","Bulgaria","Croatia","Czech_Republic","Denmark","Estonia","Finland","France","Germany","Great_Britain","Greece","Hungary","Iceland","Italy","Latvia","Liechtenstein","Lithuania","Luxembourg","Macedonia","Moldova","Norway","Netherlands","Poland","Romania","Serbia","Slovakia","Slovenia","Sweden","Switzerland","Turkey"};
    private static String[] counties = new String[]{"Austria","Belgium","Bulgaria","Czech_Republic","Denmark","Germany","Greece","Hungary","Liechtenstein","Lithuania","Luxembourg","Macedonia","Moldova","Netherlands","Poland","Romania","Serbia","Slovakia","Slovenia",};
    //private final static Pattern JSON_PATTERN = Pattern.compile("(<script type=\"text/javascript\">\\s*var data = ((.|\r\\n)*)\\s*var)");

    public static void main( String[] args ) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String outFile = args[0];
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(App.class.getResourceAsStream("/root.xml"));
        List<Poi> poiList = retrieveAllData(db);
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

    private static List<Poi> parseUkraineKlm(DocumentBuilder db) throws IOException, SAXException {
        List<Poi> pois = new ArrayList<>();
        Document ukraine = db.parse(App.class.getResourceAsStream("/UKRAINE.kml"));
        Element document = (Element) ukraine.getDocumentElement().getElementsByTagName("Document").item(0);
        Element folder = (Element) document.getElementsByTagName("Folder").item(0);
        NodeList placeMarks = folder.getElementsByTagName("Placemark");
        for(int i=0; i < placeMarks.getLength(); i++){
            Node placeMark = placeMarks.item(i);
            pois.add(new Poi((Element) placeMark));
        }
        return pois;
    }

    private static List<Poi> retrieveAllData(DocumentBuilder db) throws IOException, SAXException {
        List<Poi> result = parseUkraineKlm(db);
        for (String country: counties) {
            HttpGet httpGet = new HttpGet("http://cngeurope.com/mobapp/mapsearch/?poi=" + country.toLowerCase());
            System.out.println("Processing: " + country);
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpResponse response = httpclient.execute(httpGet);
                String str = EntityUtils.toString(response.getEntity());
                str = str.substring(str.indexOf("var data = {\"cng\":") + 18);
                str = str.substring(0, str.indexOf("};"));

                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                ScriptObjectMirror objectMirror = (ScriptObjectMirror) engine.eval(String.format("(function() {return %s}());", str));
                result.addAll(objectMirror.values().stream().map(o -> new Poi((Map<String, String>) o)).collect(Collectors.toList()));

            } catch (IOException | ScriptException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
