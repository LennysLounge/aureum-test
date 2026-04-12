package io.github.lennyslounge.aureum;

import io.github.lennyslounge.aureum.naming.FileNamingStrategy;
import io.github.lennyslounge.aureum.util.TestMethodUtil;
import org.opentest4j.AssertionFailedError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultiSectionGoldenMaster implements AutoCloseable {

    private final GoldenMaster master;
    private final Path approvedPath;
    private final Path receivedPath;
    private boolean failAtEnd = false;
    private String receivedAccumulator = "";
    private int sectionCounter = 0;
    private boolean firstRun = false;
    private Integer sectionThatFailed;
    private String sectionNameThatFailed;

    MultiSectionGoldenMaster(GoldenMaster master) {
        this.master = master;

        Method currentTestMethod = TestMethodUtil.findCurrentTestMethod();
        Path basePath = Paths.get(".")
                .resolve(System.getProperty("aureum.basePath", "."))
                .normalize();
        approvedPath = master.resolveFileName(FileNamingStrategy.Role.APPROVED, null, currentTestMethod, basePath);
        try {
            if (!Files.exists(approvedPath)) {
                firstRun = true;
                Files.createFile(approvedPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        receivedPath = master.resolveFileName(FileNamingStrategy.Role.RECEIVED, null, currentTestMethod, basePath);

        if (master.reporter != null) {
            master.reporter.readConfig(new Config());
        }
    }

    private MultiSectionGoldenMaster(
            GoldenMaster master,
            Path approvedPath,
            Path receivedPath,
            boolean failAtEnd,
            String receivedAccumulator,
            int sectionCounter,
            boolean firstRun,
            Integer sectionThatFailed,
            String sectionNameThatFailed
    ) {
        this.master = master;
        this.approvedPath = approvedPath;
        this.receivedPath = receivedPath;
        this.failAtEnd = failAtEnd;
        this.receivedAccumulator = String.valueOf(receivedAccumulator);
        this.sectionCounter = sectionCounter;
        this.firstRun = firstRun;
        this.sectionThatFailed = sectionThatFailed;
        this.sectionNameThatFailed = sectionNameThatFailed;
    }

    public MultiSectionGoldenMaster withFailAtEnd(boolean failAtEnd) {
        return new MultiSectionGoldenMaster(
                master,
                approvedPath,
                receivedPath,
                failAtEnd,
                receivedAccumulator,
                sectionCounter,
                firstRun,
                sectionThatFailed,
                sectionNameThatFailed
        );
    }

    public void verify(Object candidate) {
        verify(candidate, null);
    }

    public void verify(Object candidate, String name) {
        verify(master.serialize(candidate), name);
    }

    public void verify(String received) {
        verify(received, null);
    }

    public void verify(String received, String sectionName) {
        if (sectionCounter > 0) {
            receivedAccumulator += "--------------------------------------------------" + System.lineSeparator();
        }
        receivedAccumulator += String.format(">>> [Section %d%s%s]%n" +
                        "%s%n",
                sectionCounter,
                sectionName != null ? " : " : "",
                sectionName != null ? sectionName : "",
                received
        );

        if (!isPartiallyEqual(approvedPath, receivedAccumulator)) {

            String executionStoppedDisclaimer = String.format("Section %d%s deviated from the approved file.%n"
                            + "To prevent misleading results, subsequent sections were not executed.%n"
                            + "%n"
                            + "To continue execution despite failures use:%n"
                            + "`.withFailAtEnd(true)`",
                    sectionCounter,
                    sectionName != null ? " \"" + sectionName + "\"" : ""
            );
            receivedAccumulator += System.lineSeparator();
            receivedAccumulator += "⚠ VERIFICATION WAS HALTED ⚠" + System.lineSeparator();
            receivedAccumulator += executionStoppedDisclaimer;

            if (!failAtEnd && !firstRun) {
                master.openReporterAndThrow(approvedPath, receivedPath, receivedAccumulator,
                        executionStoppedDisclaimer
                );
            } else {
                if (sectionThatFailed == null) {
                    sectionThatFailed = sectionCounter;
                    sectionNameThatFailed = sectionName;
                }
            }
        }

        sectionCounter++;
    }

    private boolean isPartiallyEqual(Path approvedPath, String received) {
        try (BufferedReader approvedReader = Files.newBufferedReader(approvedPath, StandardCharsets.UTF_8);
             BufferedReader receivedReader = new BufferedReader(new StringReader(received))) {
            while (true) {
                String receivedLine = receivedReader.readLine();
                // If the received file is done, it's a match. A different
                // section might later match the rest of the approved file.
                if (receivedLine == null) {
                    return true;
                }

                String approvedLine = approvedReader.readLine();
                // If the approved file is done but the received file isn't then it's a mismatch.
                if (approvedLine == null) {
                    return false;
                }

                if (master.ignoreTrailingWhitespace) {
                    approvedLine = approvedLine.trim();
                    receivedLine = receivedLine.trim();
                }

                // Compare the normalized lines
                if (!approvedLine.equals(receivedLine)) {
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path resolveFileName(FileNamingStrategy.Role role) {
        return master.resolveFileName(role);
    }

    @Override
    public void close() {
        if (failAtEnd || firstRun) {
            if (!master.isEqual(approvedPath, receivedAccumulator)) {
                String subline = null;
                if (sectionThatFailed != null) {
                    subline = String.format("First deviation from approved file found in section %d %s",
                            sectionThatFailed,
                            sectionNameThatFailed != null ? String.format("\"%s\"", sectionNameThatFailed) : ""
                    );
                }
                master.openReporterAndThrow(approvedPath, receivedPath, receivedAccumulator, subline);
            }
        }
    }
}
