package io.github.lennyslounge.aureum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class Serializer {

    public Writer<Object> defaultWriter = new ToStringWriter();
    public Map<Class<?>, Writer<Object>> classWriters = new HashMap<>();

    public Serializer(Writer<Object> defaultWriter, Map<Class<?>, Writer<Object>> classWriters) {
        this.classWriters = classWriters;
        this.defaultWriter = defaultWriter;
    }

    public String toString(Object o) {
        Writer<Object> classWriter = classWriters.get(o.getClass());
        if (classWriter != null) {
            return classWriter.apply(this, o);
        }
        return defaultWriter.apply(this, o);
    }

    public static class ToStringWriter implements Writer<Object> {

        @Override
        public String apply(Serializer serializer, Object o) {
            return Objects.toString(o);
        }
    }

}
