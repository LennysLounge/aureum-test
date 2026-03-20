package io.github.lennyslounge;

import java.lang.reflect.Method;
import java.nio.file.Path;

@FunctionalInterface
public interface FileNamingStrategy {

    /**
     * Resolves the path of the approved file relative to the base path.
     *
     * @param testMethod the currently executing test method
     * @param name       an optional name for the verification, or {@code null} if not provided
     * @return path relative to the base path
     */
    Path resolve(Method testMethod, String name);
}