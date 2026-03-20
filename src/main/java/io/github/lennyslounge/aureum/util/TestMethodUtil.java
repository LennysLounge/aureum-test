package io.github.lennyslounge.aureum.util;

import java.lang.reflect.Method;
import java.util.*;

public class TestMethodUtil {
    private static final Set<String> TEST_ANNOTATION_NAMES = new HashSet<>(Arrays.asList(
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.RepeatedTest",
            "org.junit.Test"
    ));

    private static final Map<String, Method> methodCache = new HashMap<>();

    public static Method findCurrentTestMethod() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (int i = 1, stackTraceLength = stackTrace.length; i < stackTraceLength; i++) {
            StackTraceElement element = stackTrace[i];

            boolean isInternal = element.getClassName().startsWith("java.")
                    || element.getClassName().startsWith("sun.");
            if (isInternal) {
                continue;
            }

            String cacheKey = element.getClassName() + "." + element.getMethodName();
            if (methodCache.containsKey(cacheKey)) {
                Method method = methodCache.get(cacheKey);
                if (method == null) {
                    continue;
                }
                return method;
            }


            try {
                Class<?> clazz = Class.forName(element.getClassName());

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(element.getMethodName())) {

                        boolean isTest = Arrays.stream(method.getAnnotations())
                                .anyMatch(ann -> TEST_ANNOTATION_NAMES.contains(ann.annotationType().getName()));
                        if (isTest) {
                            methodCache.put(cacheKey, method);
                            return method;
                        } else {
                            methodCache.put(cacheKey, null);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // Realistically this should almost never happen.
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("No test method was found");
    }
}