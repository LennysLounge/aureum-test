package io.github.lennyslounge.aureum;

import io.github.lennyslounge.aureum.naming.FileNamePattern;
import io.github.lennyslounge.aureum.naming.FileNamingStrategy;
import io.github.lennyslounge.aureum.reporter.IntelliJDiffReporter;
import io.github.lennyslounge.aureum.reporter.Reporter;
import io.github.lennyslounge.aureum.reporter.SimpleDiffReporter;
import io.github.lennyslounge.aureum.util.TestMethodUtil;
import io.github.lennyslounge.aureum.writer.CommonWriters;
import io.github.lennyslounge.aureum.writer.ToStringWriter;
import io.github.lennyslounge.aureum.writer.Writer;
import org.opentest4j.AssertionFailedError;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoldenMaster {

        /*
    0) Configuration is immutable and threadsafe
        a) Config is just an immutable threadsafe object that is used to run validations
        b) Different environments (ci / local) can configure the config
            differently in a static block using normal code and save it as a
            public static final variable
    1) Where is the golden master file and what is it called
        a) Base search path:
            * base path = working directory + system property "aureum.basePath" (relative, defaults to ".")
        b) File name pattern
            * custom interface implementation (FileNamingStrategy)
            * builder (StandardFileNamingStrategy.builder())
        c) File extension
            * fixed
            * auto (serializer decides)
    2) How is the candidate serialized
        a) Object::toString()
        b) Structural serializers
            * Reflection based
            * json
            * xml
            * can include sanitization
                - ignore/replace fields with name x
                - ignore/replace fields with type y
                - replace with a relative offset to first occurrence
        c) Custom serializers that implement an interface
        d) one root serializer for everything
        e) custom overrides for specific types
        f) Sorting is the users responsibility
    3) Textual sanitization
        a) regex replacement
    4) Comparator
        a) Text based
            * ignore different line endings
            * ignore trailing white space
        b) Byte based
        c) Pdf based
            * ignore some parts (meta data)
            * tolerance on element placement
            * sanitization
        d) image based
            * tolerance
    5) What should happen when the output does not match the master
        a) Where should the output be written to
            * Repeat of point 1 with some modifications
                - base path same as master or custom
                - file name pattern same as master or custom
                - file extension same as master or custom
            * temporary file
            * ci artifact directory
            * Replace master
                - (in this case, rerunning the test with a dirty
                    master should always fail the test with "master is dirty")
            * multiple or none of these
        b) How should the test failure be reported
            * Opening diff tool
                - vscode
                - intelliJ
                - eclipse
                - common diff tools
            * Printing diff to output
            * multiple or none of these
     */

    public static final GoldenMaster DEFAULT_CONFIG = new GoldenMaster()
            .withFileNamingStrategy(new FileNamePattern()
                    .fixed("src/test/java/")
                    .packageAsPath()
                    .className()
                    .methodNameWithPrefix(".")
                    .verificationNameWithPrefixIfPresent(".")
                    .roleWithPrefix(".")
                    .fileExtension("txt"))
            .withFallbackWriter(new ToStringWriter())
            .withCommonWriters()
            .withReporter(new IntelliJDiffReporter());

    private final FileNamingStrategy namingStrategy;
    private final Writer<Object> fallbackWriter;
    private final Map<Class<?>, Writer<Object>> classWriters;
    private final Map<Class<?>, Writer<Object>> subclassWriters;
    private final boolean ignoreTrailingWhitespace;
    private final Reporter reporter;

    public GoldenMaster() {
        this.namingStrategy = new FileNamePattern().fixed("master");
        this.fallbackWriter = new ToStringWriter();
        this.classWriters = new HashMap<>();
        this.subclassWriters = new HashMap<>();
        this.ignoreTrailingWhitespace = false;
        this.reporter = null;
    }

    private GoldenMaster(FileNamingStrategy namingStrategy,
                         Writer<Object> fallbackWriter,
                         Map<Class<?>, Writer<Object>> classWriters,
                         Map<Class<?>, Writer<Object>> subclassWriters,
                         boolean ignoreTrailingWhitespace,
                         Reporter reporter) {
        this.namingStrategy = namingStrategy;
        this.fallbackWriter = fallbackWriter;
        this.classWriters = classWriters;
        this.subclassWriters = subclassWriters;
        this.ignoreTrailingWhitespace = ignoreTrailingWhitespace;
        this.reporter = reporter;
    }

    public static GoldenMaster defaultConfig() {
        return DEFAULT_CONFIG;
    }

    public GoldenMaster withFileNamingStrategy(FileNamingStrategy strategy) {
        return new GoldenMaster(strategy, fallbackWriter, classWriters, subclassWriters, ignoreTrailingWhitespace, reporter);
    }

    public GoldenMaster withFallbackWriter(Writer<Object> writer) {
        return new GoldenMaster(namingStrategy, writer, classWriters, subclassWriters, ignoreTrailingWhitespace, reporter);
    }

    public <T> GoldenMaster withWriterForClass(Class<T> type, Writer<T> writer) {
        Map<Class<?>, Writer<Object>> classWriters = new HashMap<>(this.classWriters);
        @SuppressWarnings("unchecked")
        Writer<Object> objectWriter = (Writer<Object>) writer;
        classWriters.put(type, objectWriter);
        return new GoldenMaster(namingStrategy, fallbackWriter, classWriters, subclassWriters, ignoreTrailingWhitespace, reporter);
    }

    public <T> GoldenMaster withWriterForSubclassOf(Class<T> type, Writer<T> writer) {
        Map<Class<?>, Writer<Object>> subclassWriters = new HashMap<>(this.subclassWriters);
        @SuppressWarnings("unchecked")
        Writer<Object> objectWriter = (Writer<Object>) writer;
        subclassWriters.put(type, objectWriter);
        return new GoldenMaster(namingStrategy, fallbackWriter, classWriters, subclassWriters, ignoreTrailingWhitespace, reporter);
    }

    public GoldenMaster withCommonWriters() {
        return this
                .withWriterForClass(String.class, CommonWriters::String)
                .withWriterForClass(Integer.class, CommonWriters::Integer)
                .withWriterForClass(Boolean.class, CommonWriters::Boolean)
                .withWriterForSubclassOf(List.class, CommonWriters::List);
    }

    public GoldenMaster withIgnoreTrailingWhitespace() {
        return new GoldenMaster(namingStrategy, fallbackWriter, classWriters, subclassWriters, true, reporter);
    }

    public GoldenMaster withReporter(Reporter reporter) {
        return new GoldenMaster(namingStrategy, fallbackWriter, classWriters, subclassWriters, ignoreTrailingWhitespace, reporter);
    }

    public void verify(Object candidate) {
        verify(candidate, null);
    }

    public void verify(Object candidate, String name) {
        Serializer serializer = new Serializer(fallbackWriter, classWriters, subclassWriters);
        verify(serializer.toString(candidate), name);
    }

    public void verify(String received) {
        verify(received, null);
    }

    public void verify(String received, String name) {
        Method currentTestMethod = TestMethodUtil.findCurrentTestMethod();

        Path basePath = Paths.get(".")
                .resolve(System.getProperty("aureum.basePath", "."))
                .normalize();

        Path masterPath = basePath.resolve(namingStrategy.resolve(new FileNamingStrategy.Context(currentTestMethod, name, FileNamingStrategy.Role.APPROVED)));
        try {
            if (!Files.exists(masterPath)) {
                Files.createFile(masterPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!isEqual(masterPath, received)) {
            Path receivedPath = basePath.resolve(namingStrategy.resolve(new FileNamingStrategy.Context(currentTestMethod,
                    name,
                    FileNamingStrategy.Role.RECEIVED)));
            try {
                Files.write(receivedPath, received.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (reporter != null) {
                reporter.report(masterPath, receivedPath);
            }
            String approved;
            try {
                approved = new String(Files.readAllBytes(masterPath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            throw new AssertionFailedError("Master does not match received", approved, received);
        }
    }

    public boolean isEqual(Path approvedFile, String received) {
        // Wrap streams in Readers to handle UTF-8 decoding and line breaking safely
        try (BufferedReader approvedReader = Files.newBufferedReader(approvedFile, StandardCharsets.UTF_8);
             BufferedReader receivedReader = new BufferedReader(new StringReader(received))) {

            String approvedLine;
            String receivedLine;

            while (true) {
                approvedLine = approvedReader.readLine();
                receivedLine = receivedReader.readLine();

                // If both reached the end of the file simultaneously, it's a match
                if (approvedLine == null && receivedLine == null) {
                    return true;
                }

                // If one file is longer than the other, it's a mismatch
                if (approvedLine == null || receivedLine == null) {
                    return false;
                }

                if (ignoreTrailingWhitespace) {
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
        return resolveFileName(role, null);
    }

    public Path resolveFileName(FileNamingStrategy.Role role, String name) {
        return namingStrategy.resolve(new FileNamingStrategy.Context(TestMethodUtil.findCurrentTestMethod(), name, role));
    }
}
