package org.tomitribe.common;

import java.io.InputStream;
import java.util.Scanner;

public class TemplateUtil {
    public static String readTemplate(String fileName) {

        StringBuilder result = new StringBuilder("");

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("templates/" + fileName);

        try (Scanner scanner = new Scanner(is)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        }

        return result.toString();

    }
}
