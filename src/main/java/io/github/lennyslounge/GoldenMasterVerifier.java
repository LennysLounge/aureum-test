package io.github.lennyslounge;

import io.github.lennyslounge.util.TestMethodUtil;
import org.opentest4j.AssertionFailedError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
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
            * by default the working directory is assumed to be the project root (where the pom lives)
            * the base path must be configurable from outside the JVM to allow for different environments
        b) File name pattern
            * custom interface implementation
            * builder
        c) File extension
            * fixed
            * auto (serializer decides
        d) Counter for multiple validations per test
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

    public void verify(String actual) {
        Method currentTestMethod = TestMethodUtil.findCurrentTestMethod();
        Class<?> currentTestClass = currentTestMethod.getDeclaringClass();
        Path basePath = Paths.get("src", "test", "java")
                .resolve(Paths.get(currentTestClass.getPackage().getName().replace(".", File.separator)));

        Path masterPath = basePath
                .resolve(currentTestClass.getSimpleName() + "." + currentTestMethod.getName() + ".approved.txt");
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
                Path receivedPath = basePath
                        .resolve(currentTestClass.getSimpleName() + "." + currentTestMethod.getName() + ".received.txt");
                Files.write(receivedPath, actual.getBytes());
                throw new AssertionFailedError("Master does not match received");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
