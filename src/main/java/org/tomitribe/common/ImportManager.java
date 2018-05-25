package org.tomitribe.common;

import java.util.HashMap;

public class ImportManager {
    private static HashMap<String, String> importMap = new HashMap<>();

    static {
        importMap.put("Date", "java.util.Date");
        importMap.put("Builder", "lombok.Builder");
        importMap.put("Collection", "java.util.Collection");
        importMap.put("Operation", "io.swagger.v3.oas.annotations.Operation");
        importMap.put("Generated", "javax.annotation.Generated");
        importMap.put("Path", "javax.ws.rs.Path");
        importMap.put("Response", "javax.ws.rs.core.Response");
        importMap.put("POST", "javax.ws.rs.POST");
        importMap.put("GET", "javax.ws.rs.GET");
        importMap.put("PUT", "javax.ws.rs.PUT");
        importMap.put("DELETE", "javax.ws.rs.DELETE");
        importMap.put("EqualsAndHashCode", "lombok.EqualsAndHashCode");
        importMap.put("AllArgsConstructor", "lombok.AllArgsConstructor");
        importMap.put("Value", "lombok.Value");
        importMap.put("Schema", "io.swagger.v3.oas.annotations.media.Schema");
        importMap.put("List", "java.util.List");
        importMap.put("Data", "lombok.Data");
        importMap.put("Produces", "javax.ws.rs.Produces");
        importMap.put("Consumes", "javax.ws.rs.Consumes");
        importMap.put("MediaType", "javax.ws.rs.core.MediaType");
    }

    public static String getImport(String className) {
        return importMap.get(className);
    }
}
