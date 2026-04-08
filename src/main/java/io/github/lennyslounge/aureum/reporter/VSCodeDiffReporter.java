package io.github.lennyslounge.aureum.reporter;

import io.github.lennyslounge.aureum.Config;
import io.github.lennyslounge.aureum.util.Os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class VSCodeDiffReporter implements Reporter {

    private boolean enabled;

    @Override
    public Result report(Path approvedFile, Path receivedFile) {
        if (!enabled) {
            return Result.FAILED;
        }
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

    @Override
    public void readConfig(Config config) {
        enabled = config.getBoolean(
                "aureum.VSCodeDiffReporter.enabled",
                true,
                "Whether this reporter will be used to report a failed verification"
        );
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
