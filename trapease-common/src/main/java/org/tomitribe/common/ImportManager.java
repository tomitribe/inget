package org.tomitribe.common;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Optional;

public class ImportManager {
    private static HashMap<String, String> importMap = new HashMap<>();

    static {
        // Java
        importMap.put("Date", "java.util.Date");
        importMap.put("Collection", "java.util.Collection");
        importMap.put("Generated", "javax.annotation.Generated");
        importMap.put("List", "java.util.List");

        // Lombok
        importMap.put("Builder", "lombok.Builder");
        importMap.put("EqualsAndHashCode", "lombok.EqualsAndHashCode");
        importMap.put("AllArgsConstructor", "lombok.AllArgsConstructor");
        importMap.put("Value", "lombok.Value");
        importMap.put("Data", "lombok.Data");

        // Swagger
        importMap.put("Operation", "io.swagger.v3.oas.annotations.Operation");
        importMap.put("Schema", "io.swagger.v3.oas.annotations.media.Schema");

        // JAX-RS
        importMap.put("Path", "javax.ws.rs.Path");
        importMap.put("PathParam", "javax.ws.rs.PathParam");
        importMap.put("Response", "javax.ws.rs.core.Response");
        importMap.put("POST", "javax.ws.rs.POST");
        importMap.put("GET", "javax.ws.rs.GET");
        importMap.put("PUT", "javax.ws.rs.PUT");
        importMap.put("DELETE", "javax.ws.rs.DELETE");
        importMap.put("Produces", "javax.ws.rs.Produces");
        importMap.put("Consumes", "javax.ws.rs.Consumes");
        importMap.put("MediaType", "javax.ws.rs.core.MediaType");

        // Johnzon
        importMap.put("JohnzonProvider", "org.apache.johnzon.jaxrs.JohnzonProvider");

        // CXF - Microprofile
        importMap.put("RestClientBuilder", "org.eclipse.microprofile.rest.client.RestClientBuilder");

        // Airlift
        importMap.put("Option", "io.airlift.airline.Option");
        importMap.put("Command", "io.airlift.airline.Command");
        importMap.put("Arguments", "io.airlift.airline.Arguments");
    }

    public static String getImport(String className) {
        return importMap.get(className);
    }

    public static String getImportFromClass(CompilationUnit rootClassUnit, Type commonType) {
        Optional<ImportDeclaration> imp = rootClassUnit.getImports().stream().filter(i -> i.getNameAsString().endsWith("." + commonType)).findFirst();

        if (imp.isPresent()) {
            return imp.get().getNameAsString();
        }
        return null;
    }
}
