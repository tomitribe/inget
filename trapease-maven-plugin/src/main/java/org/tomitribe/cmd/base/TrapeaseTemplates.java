package org.tomitribe.cmd.base;

public class TrapeaseTemplates {

    public static String TRAPEASE_COMMAND = "import io.airlift.airline.Option;\n" +
            "import io.airlift.airline.OptionType;\n" +
            "import resources.client.base.ClientConfiguration;\n" +
            "import java.net.URL;\n" +
            "\n" +
            "public abstract class TrapeaseCommand implements Runnable {\n" +
            "    @Option(name = {\"-l\", \"--url\"}, type = OptionType.GLOBAL)\n" +
            "    private URL url;\n" +
            "\n" +
            "    @Option(name = {\"-v\", \"--verbose\"}, type = OptionType.GLOBAL)\n" +
            "    private boolean verbose;\n" +
            "\n" +
            "    @Override\n" +
            "    public final void run() {\n" +
            "        final ClientConfiguration clientConfiguration =\n" +
            "                ClientConfiguration.builder()\n" +
            "                                   .url(url)\n" +
            "                                   .verbose(verbose)\n" +
            "                                   .build();\n" +
            "        run(clientConfiguration);\n" +
            "    }\n" +
            "\n" +
            "    protected abstract void run(final ClientConfiguration clientConfiguration);\n" +
            "}";

    public static String TRAPEASE_CLI = "import io.airlift.airline.Cli;\n" +
            "import io.airlift.airline.Help;\n" +
            "\n" +
            "public class TrapeaseCli {\n" +
            "    public static void main(String[] args) {\n" +
            "        final Cli.CliBuilder<Runnable> trapease = Cli.builder(\"trapease\");\n" +
            "        trapease.withDefaultCommand(Help.class);\n" +
            "        trapease.withCommand(Help.class);\n" +
            "\n" +
            "        commands(trapease);\n" +
            "\n" +
            "        final Cli<Runnable> cli = trapease.build();\n" +
            "        cli.parse(args).run();\n" +
            "    }\n" +
            "}";
}
