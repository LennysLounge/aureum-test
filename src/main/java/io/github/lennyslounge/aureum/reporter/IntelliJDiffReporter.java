package io.github.lennyslounge.aureum.reporter;

import java.io.IOException;
import java.nio.file.Path;

public class IntelliJDiffReporter implements Reporter {

    @Override
    public void report(Path approvedFile, Path receivedFile) {
        try {
            new ProcessBuilder(
                     "open",
                     "-na",
                     "IntelliJ IDEA.app",
                     "--args",
                     "diff",
                     approvedFile.toAbsolutePath().toString(),
                     receivedFile.toAbsolutePath().toString()
            ).start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open IntelliJ diff viewer", e);
        }
        System.out.println("Diff opened in IntelliJ");
    }
}
