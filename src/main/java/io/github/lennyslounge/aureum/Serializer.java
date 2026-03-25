package io.github.lennyslounge.aureum;

import io.github.lennyslounge.aureum.writer.Writer;

import java.util.HashMap;
import java.util.Map;

public class Serializer {

    Writer<Object> defaultWriter = null;
    Map<Class<?>, Writer<Object>> classWriters = new HashMap<>();
    Map<Class<?>, Writer<Object>> subclassWriters = new HashMap<>();
    private int indentLevel = 0;
    private String indent = "";

    Serializer(Writer<Object> defaultWriter,
               Map<Class<?>, Writer<Object>> classWriters,
               Map<Class<?>, Writer<Object>> subclassWriters) {
        this.classWriters = classWriters;
        this.defaultWriter = defaultWriter;
        this.subclassWriters = subclassWriters;
    }

    public String toString(Object o) {
        Writer<Object> classWriter = classWriters.get(o.getClass());
        if (classWriter != null) {
            return classWriter.apply(this, o);
        }
        for (Class<?> subclass : subclassWriters.keySet()) {
            if (subclass.isAssignableFrom(o.getClass())) {
                Writer<Object> writer = subclassWriters.get(subclass);
                return writer.apply(this, o);
            }
        }
        return defaultWriter.apply(this, o);
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
}
