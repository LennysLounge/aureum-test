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
