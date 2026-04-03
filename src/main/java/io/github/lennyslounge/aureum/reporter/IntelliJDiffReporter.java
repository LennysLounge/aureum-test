package io.github.lennyslounge.aureum.reporter;

import io.github.lennyslounge.aureum.util.Os;

import java.io.IOException;
import java.nio.file.Path;

public class IntelliJDiffReporter implements Reporter {

    @Override
    public Reporter.Result report(Path approvedFile, Path receivedFile) {
        switch (Os.getOs()) {
            case WINDOWS:
                openWindows(approvedFile, receivedFile);
                return Result.SUCCESS;
            case MAC:
                openMac(approvedFile, receivedFile);
                return Result.SUCCESS;
            case OTHER:
            default:
                return Result.FAILED;
        }
    }

    private void openWindows(Path approvedFile, Path receivedFile) {
        try {
            new ProcessBuilder(
                    "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2024.3.2.2\\bin\\idea64.exe",
                    "diff",
                    approvedFile.toAbsolutePath().toString(),
                    receivedFile.toAbsolutePath().toString()
            ).start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open IntelliJ diff viewer", e);
        }
    }

    private void openMac(Path approvedFile, Path receivedFile) {
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
    }
}
