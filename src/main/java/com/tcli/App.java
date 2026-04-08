package com.tcli;

import com.tcli.cli.ParseCommand;
import com.tcli.cli.InstallCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "tcli",
    mixinStandardHelpOptions = true,
    version = "tcli 0.1.0",
    description = "Convert any file format to text and extract metadata.",
    subcommands = {ParseCommand.class, InstallCommand.class}
)
public class App implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        // Force headless mode before any AWT class loads (needed for PDF on macOS native-image)
        System.setProperty("java.awt.headless", "true");
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
