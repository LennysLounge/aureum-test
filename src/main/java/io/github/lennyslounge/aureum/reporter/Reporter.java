package io.github.lennyslounge.aureum.reporter;

import java.nio.file.Path;

public interface Reporter {
    Result report(Path approvedFile, Path receivedFile);

    enum Result {
        SUCCESS,
        FAILED;
    }
}
