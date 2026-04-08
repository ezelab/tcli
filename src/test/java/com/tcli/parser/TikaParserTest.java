package com.tcli.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TikaParserTest {

    @Test
    void parseTextFromInputStream() throws Exception {
        String content = "Hello, world!";
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        TikaParser parser = new TikaParser(false, 0);
        TikaParser.ParseResult result = parser.parse(is, "test.txt");

        assertNotNull(result);
        assertTrue(result.getText().contains("Hello, world!"));
        assertEquals("test.txt", result.getName());
    }

    @Test
    void parseHtmlContent() throws Exception {
        String html = "<html><body><p>Hello HTML</p></body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));

        TikaParser parser = new TikaParser(false, 0);
        TikaParser.ParseResult result = parser.parse(is, "test.html");

        assertNotNull(result);
        assertTrue(result.getText().contains("Hello HTML"));
    }

    @Test
    void parseResultContainsMetadata() throws Exception {
        String content = "Some content";
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        TikaParser parser = new TikaParser(false, 0);
        TikaParser.ParseResult result = parser.parse(is, "test.txt");

        assertNotNull(result.getMetadata());
        assertFalse(result.getMetadata().isEmpty());
    }

    @Test
    void parseWithNoRecursionHasNoChildren() throws Exception {
        String content = "plain text";
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        TikaParser parser = new TikaParser(false, 0);
        TikaParser.ParseResult result = parser.parse(is, "test.txt");

        assertTrue(result.getChildren().isEmpty());
    }
}
