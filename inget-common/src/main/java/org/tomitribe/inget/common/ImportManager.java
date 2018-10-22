/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.tomitribe.inget.common;

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
        importMap.put("ToString", "lombok.ToString");
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
        importMap.put("QueryParam", "javax.ws.rs.QueryParam");
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
        importMap.put("OutInterceptors", "org.apache.cxf.interceptor.OutInterceptors");

        // Airlift
        importMap.put("Option", "io.airlift.airline.Option");
        importMap.put("Command", "io.airlift.airline.Command");
        importMap.put("Arguments", "io.airlift.airline.Arguments");
        importMap.put("Cli", "io.airlift.airline.Cli");
        importMap.put("Help", "io.airlift.airline.Help");

        // Inget
        importMap.put("ClientConfiguration", "org.tomitribe.inget.client.ClientConfiguration");
        importMap.put("BasicConfiguration", "org.tomitribe.inget.client.BasicConfiguration");
        importMap.put("SignatureConfiguration", "org.tomitribe.inget.client.SignatureConfiguration");
        importMap.put("SignatureAuthenticator", "org.tomitribe.inget.client.SignatureAuthenticator");
        importMap.put("BasicAuthenticator", "org.tomitribe.inget.client.BasicAuthenticator");
        importMap.put("LogClientRequestFilter", "org.tomitribe.inget.client.LogClientRequestFilter");
        importMap.put("LogClientResponseFilter", "org.tomitribe.inget.client.LogClientResponseFilter");
        importMap.put("NoOpInterceptor", "org.tomitribe.inget.client.NoOpInterceptor");
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
