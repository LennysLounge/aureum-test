package io.github.lennyslounge.aureum;

import io.github.lennyslounge.aureum.util.TestMethodUtil;
import org.opentest4j.AssertionFailedError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class GoldenMasterVerifier {

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

    private final FileNamingStrategy namingStrategy;
    private final Serializer serializer;

    public GoldenMasterVerifier() {
        this(ctx -> Paths.get(""),
                new Serializer(new Serializer.ToStringWriter(), new HashMap<>())
        );
    }

    private GoldenMasterVerifier(FileNamingStrategy namingStrategy, Serializer serializer) {
        this.namingStrategy = namingStrategy;
        this.serializer = serializer;
    }

    public GoldenMasterVerifier withFileNamingStrategy(FileNamingStrategy strategy) {
        return new GoldenMasterVerifier(strategy, serializer);
    }

    public GoldenMasterVerifier withFallbackWriter(Writer<Object> writer) {
        return new GoldenMasterVerifier(namingStrategy,
                new Serializer(writer, new HashMap<>(serializer.classWriters))
        );
    }

    public GoldenMasterVerifier withFallbackWriter(BiFunction<Serializer, Object, String> writer) {
        return withFallbackWriter(writer::apply);
    }

    public <T> GoldenMasterVerifier withWriterForClass(Class<T> type, Writer<T> writer) {
        Map<Class<?>, Writer<Object>> classWriters = new HashMap<>(serializer.classWriters);
        @SuppressWarnings("unchecked")
        Writer<Object> objectWriter = (Writer<Object>) writer;
        classWriters.put(type, objectWriter);
        return new GoldenMasterVerifier(namingStrategy,
                new Serializer(serializer.defaultWriter, classWriters)
        );
    }

    public GoldenMasterVerifier withCommonWriters() {
        return this
                .withWriterForClass(String.class, (serializer, str) -> "\"" + str + "\"")
                .withWriterForClass(Integer.class, (serializer, i) -> String.valueOf(i))
                .withWriterForClass(Boolean.class, (serializer, bool) -> String.valueOf(bool))
                ;
    }

    public void verify(Object actual) {
        verify(serializer.toString(actual), null);
    }

    public void verify(Object actual, String name) {
        verify(serializer.toString(actual), name);
    }

    public void verify(String actual) {
        verify(actual, null);
    }

    public void verify(String actual, String name) {
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

        try (Stream<String> masterLines = Files.lines(masterPath);
             BufferedReader reader = new BufferedReader(new StringReader(actual))) {
            Stream<String> actualLines = reader.lines();

            Iterator<String> masterLinesIter = masterLines.iterator();
            Iterator<String> actualLinesIter = actualLines.iterator();

            boolean isEqual = true;
            while (masterLinesIter.hasNext() && actualLinesIter.hasNext()) {
                if (!masterLinesIter.next().equals(actualLinesIter.next())) {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) {
                if (masterLinesIter.hasNext() != actualLinesIter.hasNext()) {
                    isEqual = false;
                }
            }

            if (!isEqual) {
                Path receivedPath = basePath.resolve(namingStrategy.resolve(new FileNamingStrategy.Context(currentTestMethod,
                        name,
                        FileNamingStrategy.Role.RECEIVED)));
                Files.write(receivedPath, actual.getBytes());
                throw new AssertionFailedError("Master does not match received");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
