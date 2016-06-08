package org.home.poi;

import java.util.Map;

public class Poi {
    private String latitude;
    private String longitude;
    private String description;

    public Poi(Map<String, String> m) {
        latitude  = m.get("lat");
        longitude  = m.get("lon");
        description  = createDescription(m);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String createDescription(Map<String, String> m) {
        StringBuilder stringBuilder = new StringBuilder();
        addToDescription(stringBuilder, m, "city");
        addToDescription(stringBuilder, m, "addr");
        addToDescription(stringBuilder, m, "phone");
        addToDescription(stringBuilder, m, "open");
        addToDescription(stringBuilder, m, "note");
        return stringBuilder.toString();
    }

    private void addToDescription(StringBuilder stringBuilder, Map<String, String> m, String field) {
        if (m.containsKey(field) && !m.get(field).isEmpty()){
            stringBuilder.append(field);
            stringBuilder.append(" : ");
            stringBuilder.append(m.get(field));
            stringBuilder.append("\n");
        }

    }
}
