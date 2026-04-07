package io.github.lennyslounge.aureum.reporter;

import io.github.lennyslounge.aureum.util.Os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class VSCodeDiffReporter implements Reporter {

    @Override
    public Result report(Path approvedFile, Path receivedFile) {
        switch (Os.getOs()) {
            case WINDOWS:
                openWindows(approvedFile, receivedFile);
                return Result.SUCCESS;
            case MAC:
            case OTHER:
            default:
                return Result.FAILED;
        }
    }

    private void openWindows(Path approvedFile, Path receivedFile) {
        try {
            new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "code",
                    "-d",
                    approvedFile.toAbsolutePath().toString(),
                    receivedFile.toAbsolutePath().toString()
            ).start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open VSCode diff viewer", e);
        }
    }
}
