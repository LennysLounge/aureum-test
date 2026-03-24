package io.github.lennyslounge.aureum;

import io.github.lennyslounge.aureum.util.TestMethodUtil;
import org.opentest4j.AssertionFailedError;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

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
             .withComparator(new LineComparator());

    private final FileNamingStrategy namingStrategy;
    private final Writer<Object> fallbackWriter;
    private final Map<Class<?>, Writer<Object>> classWriters;
    private final Comparator comparator;

    public GoldenMaster() {
        this.namingStrategy = new FileNamePattern().fixed("master");
        this.fallbackWriter = new ToStringWriter();
        this.classWriters = new HashMap<>();
        this.comparator = new LineComparator();
    }

    private GoldenMaster(FileNamingStrategy namingStrategy,
             Writer<Object> fallbackWriter,
             Map<Class<?>, Writer<Object>> classWriters,
             Comparator comparator) {
        this.namingStrategy = namingStrategy;
        this.fallbackWriter = fallbackWriter;
        this.classWriters = classWriters;
        this.comparator = comparator;
    }

    public static GoldenMaster defaultConfig() {
        return DEFAULT_CONFIG;
    }

    public GoldenMaster withFileNamingStrategy(FileNamingStrategy strategy) {
        return new GoldenMaster(
                 strategy,
                 fallbackWriter,
                 classWriters,
                 comparator
        );
    }

    public GoldenMaster withFallbackWriter(Writer<Object> writer) {
        return new GoldenMaster(
                 namingStrategy,
                 writer,
                 classWriters,
                 comparator
        );
    }

    public <T> GoldenMaster withWriterForClass(Class<T> type, Writer<T> writer) {
        Map<Class<?>, Writer<Object>> classWriters = new HashMap<>(this.classWriters);
        @SuppressWarnings("unchecked")
        Writer<Object> objectWriter = (Writer<Object>) writer;
        classWriters.put(type, objectWriter);
        return new GoldenMaster(namingStrategy,
                 fallbackWriter,
                 classWriters,
                 comparator
        );
    }

    public GoldenMaster withCommonWriters() {
        return this
                 .withWriterForClass(String.class, (serializer, str) -> "\"" + str + "\"")
                 .withWriterForClass(Integer.class, (serializer, i) -> String.valueOf(i))
                 .withWriterForClass(Boolean.class, (serializer, bool) -> String.valueOf(bool))
                 ;
    }

    public GoldenMaster withComparator(Comparator comparator) {
        return new GoldenMaster(
                 namingStrategy,
                 fallbackWriter,
                 classWriters,
                 comparator
        );
    }

    public void verify(Object candidate) {
        verify(candidate, null);
    }

    public void verify(Object candidate, String name) {
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

        Serializer serializer = new Serializer(fallbackWriter, classWriters);
        String received = serializer.toString(candidate);

        try (InputStream approvedIS = Files.newInputStream(masterPath);
                 InputStream receivedIS = new ByteArrayInputStream(received.getBytes());
        ) {

            boolean isEqual = comparator.isEqual(approvedIS, receivedIS);
            if (!isEqual) {
                Path receivedPath = basePath.resolve(namingStrategy.resolve(new FileNamingStrategy.Context(currentTestMethod,
                         name,
                         FileNamingStrategy.Role.RECEIVED)));
                Files.write(receivedPath, received.getBytes());
                throw new AssertionFailedError("Master does not match received");
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
