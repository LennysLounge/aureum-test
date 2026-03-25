package io.github.lennyslounge.aureum.naming;

import java.lang.reflect.Method;
import java.nio.file.Path;

@FunctionalInterface
public interface FileNamingStrategy {

    Path resolve(Context context);

    enum Role {
        APPROVED, RECEIVED
    }

    class Context {
        private final Method testMethod;
        private final String name;
        private final Role role;

        public Context(Method testMethod, String name, Role role) {
            this.testMethod = testMethod;
            this.name = name;
            this.role = role;
        }

        public Method getTestMethod() {
            return testMethod;
        }

        public String getName() {
            return name;
        }

        public Role getRole() {
            return role;
        }
    }
}
