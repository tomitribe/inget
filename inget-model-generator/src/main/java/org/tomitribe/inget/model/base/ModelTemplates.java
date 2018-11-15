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
package org.tomitribe.inget.model.base;

//CHECKSTYLE:OFF
public class ModelTemplates {

        private ModelTemplates() {
                // no-op
        }

        public static final String FAILURE =
                "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                        "import lombok.AllArgsConstructor;\n" +
                        "import lombok.Value;\n" +
                        "\n" +
                        "@Value\n" +
                        "@AllArgsConstructor\n" +
                        "@Schema(description = \"Failure details\")\n" +
                        "public class Failure {\n" +
                        "\n" +
                        "    @Schema(description = \"name of the entity that failed to be updated.\", required = true)\n" +
                        "    private final String name;\n" +
                        "\n" +
                        "    @Schema(description = \"message for the failure.\", required = true)\n" +
                        "    private final String message;\n" +
                        "\n" +
                        "    @Schema(description = \"code for the failure.\", required = true)\n" +
                        "    private final String code;\n" +
                        "}";

        public static final String DEFAULT_FILTER =
                "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                        "\n" +
                        "@Schema(description = \"A generic filter, part of the page result used for any search request. \" +\n" +
                        "        \"Sub-classes contain the elements that can be used for filtering.\")\n" +
                        "public class DefaultFilter {\n" +
                        "}";

        public static final String RESULT = "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                "import lombok.Builder;\n" +
                "import lombok.EqualsAndHashCode;\n" +
                "import lombok.Value;\n" +
                "\n" +
                "import javax.annotation.Generated;\n" +
                "import java.util.Collection;\n" +
                "\n" +
                "@Value\n" +
                "@Builder\n" +
                "@EqualsAndHashCode\n" +
                "@Generated(value = \"org.tomitribe.model.ModelGenerator\")\n" +
                "@Schema(description = \"The list of %ITEMS_NAME available for a given search request with associated metadata.\")\n" +
                "public class %ENTITYResult {\n" +
                "\n" +
                "    @Schema(description = \"The list of items for the given page. The list may be a partial list when pagination is used (default)\", required = true)\n" +
                "    private final Collection<%ENTITY> items;\n" +
                "\n" +
                "    @Schema(description = \"Contains the elements that can be used for filtering: labels, by default.\", required = true)\n" +
                "    private final %FILTER filters;\n" +
                "\n" +
                "    @Schema(description = \"The total number of items for the search request. It may be higher than the number of items returned because of the pagination.\", required = true)\n" +
                "    private final Long total;\n" +
                "\n" +
                "}";
}
//CHECKSTYLE:ON