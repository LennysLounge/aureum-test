package io.github.lennyslounge.aureum.reporter;

import java.nio.file.Path;

public interface Reporter {
    void report(Path approvedFile, Path receivedFile);
}
