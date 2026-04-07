package io.github.lennyslounge.aureum.reporter;

import java.nio.file.Path;

public interface Reporter {
    Result report(Path approvedFile, Path receivedFile);

    enum Result {
        SUCCESS,
        FAILED;
    }

    class All implements Reporter {
        Reporter[] reporters;

        public All(Reporter... reporters) {
            this.reporters = reporters;
        }

        @Override
        public Result report(Path approvedFile, Path receivedFile) {
            Result result = Result.SUCCESS;
            for (Reporter reporter : reporters) {
                if (reporter.report(approvedFile, receivedFile) != Result.SUCCESS) {
                    result = Result.FAILED;
                }
            }
            return result;
        }
    }

    class FirstSuccessful implements Reporter {
        Reporter[] reporters;

        public FirstSuccessful(Reporter... reporters) {
            this.reporters = reporters;
        }

        @Override
        public Result report(Path approvedFile, Path receivedFile) {
            for (Reporter reporter : reporters) {
                if (reporter.report(approvedFile, receivedFile) == Result.SUCCESS) {
                    return Result.SUCCESS;
                }
            }
            return Result.FAILED;
        }
    }
}
