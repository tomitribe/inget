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

    public static String PAGE = "import " + Configuration.MODEL_PACKAGE + ".base.filter.DefaultFilter;\n" +
            "import io.swagger.v3.oas.annotations.media.Schema;\n" +
            "import lombok.AllArgsConstructor;\n" +
            "import lombok.Data;\n" +
            "\n" +
            "import java.util.Collection;\n" +
            "\n" +
            "// @value not possible with abstract\n" +
            "@Data\n" +
            "@AllArgsConstructor\n" +
            "@Schema(description = \"A generic page result used for any search request on server managed entities.\")\n" +
            "public abstract class Page<ITEM> {\n" +
            "\n" +
            "    @Schema(description = \"The list of items for the given page. The list may be a partial list when pagination is used (default)\", required = true)\n" +
            "    private final Collection<ITEM> items;\n" +
            "\n" +
            "    @Schema(description = \"Contains the elements that can be used for filtering: labels, by default.\", required = true)\n" +
            "    private final DefaultFilter filters;\n" +
            "\n" +
            "    @Schema(description = \"The paging state is used for paginating results call after call. It needs to be passed in again on the next search request.\", required = false)\n" +
            "    private final String pagingState;\n" +
            "\n" +
            "    @Schema(description = \"The total number of items for the search request. It may be higher than the number of items returned because of the pagination.\", required = true)\n" +
            "    private final Long total;\n" +
            "}";
}
