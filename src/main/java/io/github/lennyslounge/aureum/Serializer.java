package io.github.lennyslounge.aureum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class Serializer {

    Writer<Object> defaultWriter = null;
    Map<Class<?>, Writer<Object>> classWriters = new HashMap<>();
    private int indentLevel = 0;
    private String indent = "";

    Serializer() {

    }

    Serializer(Writer<Object> defaultWriter, Map<Class<?>, Writer<Object>> classWriters) {
        this.classWriters = classWriters;
        this.defaultWriter = defaultWriter;
    }

    Writer<Object> getWriter(Class<?> clazz) {
        Writer<Object> classWriter = classWriters.get(clazz);
        if (classWriter != null) {
            return classWriter;
        }
        return defaultWriter;
    }

    public String toString(Object o) {
        return getWriter(o.getClass()).apply(this, o);
    }

    public void increaseIndent() {
        indentLevel += 1;
        indent = indent + "    ";
    }

    public void decreaseIndent() {
        if (indentLevel > 0) {
            indentLevel -= 1;
            StringBuilder indentBuilder = new StringBuilder();
            for (int i = 0; i < indentLevel; i++) {
                indentBuilder.append("    ");
            }
            indent = indentBuilder.toString();
        }
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public String getIndent() {
        return indent;
    }

    public static class ToStringWriter implements Writer<Object> {

        @Override
        public String apply(Serializer serializer, Object o) {
            return Objects.toString(o);
        }
    }

}
