package io.github.lennyslounge.aureum.reporter;

import io.github.lennyslounge.aureum.Config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DiffReporter implements Reporter {

    private List<Reporter> diffReporters;

    public DiffReporter() {
        diffReporters = new ArrayList<>();
    }

    private DiffReporter(List<Reporter> reporters) {
        diffReporters = reporters;
    }

    @Override
    public Result report(Path approvedFile, Path receivedFile) {
        for (Reporter reporter : diffReporters) {
            if (reporter.report(approvedFile, receivedFile) == Result.SUCCESS) {
                return Result.SUCCESS;
            }
        }
        return Result.FAILED;
    }

    @Override
    public void readConfig(Config config) {
        diffReporters = config.get(
                "aureum.DiffReporter.reporters",
                diffReporters,
                this::serializeDiffReporters,
                this::deserializeDiffReporters,
                "Defines the ordered list of reporters to be used for opening a diff viewer",
                "Class names must be fully qualified and comma separated, line breaks are permitted using a \\"
        );

        diffReporters.forEach(r -> r.readConfig(config));
    }

    private String serializeDiffReporters(List<Reporter> reporters) {
        return diffReporters.stream()
                .map(r -> r.getClass().getCanonicalName())
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    private List<Reporter> deserializeDiffReporters(String s) {
        List<Reporter> reporters = new ArrayList<>();
        for (String reporterClassName : s.split(",")) {
            try {
                Class<?> clazz = Class.forName(reporterClassName);
                if (Reporter.class.isAssignableFrom(clazz)) {
                    Reporter r = (Reporter) clazz.newInstance();
                    reporters.add(r);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return reporters;
    }

    public DiffReporter withDiffReporter(Reporter reporter) {
        List<Reporter> newReporters = new ArrayList<>(diffReporters);
        newReporters.add(reporter);
        return new DiffReporter(newReporters);
    }

    public DiffReporter withCommonReporters() {
        return this
                .withDiffReporter(new VSCodeDiffReporter())
                .withDiffReporter(new IntelliJDiffReporter())
                .withDiffReporter(new SimpleDiffReporter());
    }
}
