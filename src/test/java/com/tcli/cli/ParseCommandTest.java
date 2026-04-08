package com.tcli.cli;

import com.tcli.App;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ParseCommandTest {

    @Test
    void helpShowsParseUsage() {
        StringWriter sw = new StringWriter();
        StringWriter ew = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setOut(new PrintWriter(sw));
        cmd.setErr(new PrintWriter(ew));
        cmd.execute("parse", "--help");
        String output = sw.toString() + ew.toString();
        assertTrue(output.contains("--format"));
        assertTrue(output.contains("--ocr"));
        assertTrue(output.contains("--depth"));
        assertTrue(output.contains("--metadata"));
    }

    @Test
    void missingFileReturnsError() {
        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("parse", "/nonexistent/file.pdf");
        assertEquals(1, exitCode);
    }
}
