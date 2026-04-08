package io.github.lennyslounge.aureum.reporter;

import io.github.lennyslounge.aureum.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SimpleDiffReporter implements Reporter {


    private boolean enabled;
    private int maxDiffs;

    @Override
    public Result report(Path approvedFile, Path receivedFile) {
        if (!enabled) {
            return Result.FAILED;
        }
        List<String> approved;
        List<String> received;
        try {
            approved = Files.readAllLines(approvedFile, StandardCharsets.UTF_8);
            received = Files.readAllLines(receivedFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("--- " + approvedFile);
        System.out.println("+++ " + receivedFile);

        int shownDiffs = 0;
        int remainingDiffs = 0;
        int maxLines = Math.max(approved.size(), received.size());

        for (int i = 0; i < maxLines; i++) {
            String approvedLine = i < approved.size() ? approved.get(i) : null;
            String receivedLine = i < received.size() ? received.get(i) : null;

            boolean equal = approvedLine != null && receivedLine != null && approvedLine.equals(receivedLine);
            if (equal) continue;

            if (shownDiffs >= maxDiffs) {
                remainingDiffs++;
                continue;
            }

            if (approvedLine != null) System.out.println("-" + approvedLine);
            if (receivedLine != null) System.out.println("+" + receivedLine);
            shownDiffs++;
        }

        if (remainingDiffs > 0) {
            System.out.println(remainingDiffs + " more lines differ");
        }
        return Result.SUCCESS;
    }

    @Override
    public void readConfig(Config config) {
        enabled = config.getBoolean(
                "aureum.SimpleDiffReporter.enabled",
                true,
                "Whether this reporter will be used to report a failed verification"
        );
        maxDiffs = config.getInteger(
                "aureum.SimpleDiffReporter.maxDiffs",
                10,
                "The maximum amount of diff lines shown before printing \"x more lines differ\""
        );
    }
}
