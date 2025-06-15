package ru.nsu.components.secondary;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class Helper {
    private final static Parser parser = Parser.builder().build();
    private final static StringBuilder stringBuilder = new StringBuilder();
    private final static HtmlRenderer renderer = HtmlRenderer.builder().build();

    public static String parseMarkdown(String markdown) {
        stringBuilder.setLength(0);
        stringBuilder.append("<html><body>");
        stringBuilder.append(renderer.render(parser.parse(markdown)));
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }
}
