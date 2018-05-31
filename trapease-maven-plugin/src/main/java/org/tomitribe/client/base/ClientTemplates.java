package org.tomitribe.client.base;

public class ClientTemplates {

    public static final String CLIENT_CONFIGURATION = "import lombok.AccessLevel;\n" +
            "import lombok.AllArgsConstructor;\n" +
            "import lombok.Builder;\n" +
            "import lombok.Getter;\n" +
            "\n" +
            "import java.net.URL;\n" +
            "\n" +
            "@AllArgsConstructor(access = AccessLevel.PRIVATE)\n" +
            "@Getter\n" +
            "@Builder\n" +
            "public class ClientConfiguration {\n" +
            "    private URL tagUrl;\n" +
            "    private boolean verbose;\n" +
            "}\n";
}
