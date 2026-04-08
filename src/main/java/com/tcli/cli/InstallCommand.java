package com.tcli.cli;

import com.tcli.skill.SkillManifest;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "install",
    mixinStandardHelpOptions = true,
    description = "Install tcli as a skill for coding agents."
)
public class InstallCommand implements Callable<Integer> {

    @CommandLine.Option(names = "--skill", required = true, description = "Install tcli as a coding agent skill")
    private boolean skill;

    @Override
    public Integer call() {
        try {
            if (skill) {
                SkillManifest manifest = new SkillManifest();
                manifest.install();
                System.out.println("tcli skill installed successfully.");
            }
            return 0;
        } catch (Exception e) {
            System.err.println("tcli: error installing skill: " + e.getMessage());
            return 1;
        }
    }
}
