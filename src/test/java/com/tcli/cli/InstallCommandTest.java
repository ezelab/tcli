package com.tcli.cli;

import com.tcli.App;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class InstallCommandTest {

    @Test
    void helpShowsInstallUsage() {
        StringWriter sw = new StringWriter();
        StringWriter ew = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setOut(new PrintWriter(sw));
        cmd.setErr(new PrintWriter(ew));
        cmd.execute("install", "--help");
        String output = sw.toString() + ew.toString();
        assertTrue(output.contains("--skill"));
    }
}
