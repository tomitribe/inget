package org.tomitribe.model.base;

import org.tomitribe.common.Configuration;

public class ModelTemplates {

    public static String FAILURE =
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

    public static String DEFAULT_FILTER =
            "import io.swagger.v3.oas.annotations.media.Schema;\n" +
                    "\n" +
                    "@Schema(description = \"A generic filter, part of the page result used for any search request. \" +\n" +
                    "        \"Sub-classes contain the elements that can be used for filtering.\")\n" +
                    "public class DefaultFilter {\n" +
                    "}";

    public static String RESULT = "import io.swagger.v3.oas.annotations.media.Schema;\n" +
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
