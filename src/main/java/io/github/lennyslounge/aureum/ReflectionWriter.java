package io.github.lennyslounge.aureum;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ReflectionWriter implements Writer<Object> {

    private final boolean usePrettyPrinting;

    /*
    - ignore field
    - ignore fields of type
    - replace with placeholder
        > static placeholder e.g. <UUID>
        > counting occurrences e.g.
            every occurrence of 'a' is replaced with <UUID_1>,
            every occurrence of 'b' is replaced with <UUID_2>,
            etc.
        > relative to first occurrence e.g.
            first occurrence is replaced with <TIMESTAMP_A>
            every other occurrence is replace with its value relative to the first occurrence
                <TIMESTAMP_A+12s>
                <COUNT_A+1>, <COUNT_A+2>
                etc
     */

    public ReflectionWriter() {
        this(false);
    }

    private ReflectionWriter(boolean usePrettyPrinting) {
        this.usePrettyPrinting = usePrettyPrinting;
    }

    @Override
    public String apply(Serializer serializer, Object o) {
        Class<?> clazz = o.getClass();

        StringBuilder sb = new StringBuilder();
        sb.append(getClassName(clazz))
                .append("[");

        serializer.increaseIndent();
        boolean first = true;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            FieldValue value = getValueOfField(field, o, clazz);
            if (value.isAccessable) {
                if (first) {
                    if (usePrettyPrinting) {
                        sb.append(System.lineSeparator());
                        sb.append(serializer.getIndent());
                    }
                    first = false;
                } else {
                    if (usePrettyPrinting) {
                        sb.append(",");
                        sb.append(System.lineSeparator());
                        sb.append(serializer.getIndent());
                    } else {
                        sb.append(", ");
                    }
                }
                sb.append(field.getName())
                        .append(": ")
                        .append(serializer.toString(value.value));
            }
        }
        serializer.decreaseIndent();
        if (usePrettyPrinting && !first) {
            sb.append(",");
            sb.append(System.lineSeparator());
        }
        sb.append("]");
        return sb.toString();
    }

    public ReflectionWriter withPrettyPrinting() {
        return new ReflectionWriter(true);
    }

    private static String getClassName(Class<?> clazz) {
        StringBuilder className = new StringBuilder(clazz.getSimpleName());
        Class<?> declaringClass = clazz.getDeclaringClass();
        while (declaringClass != null) {
            className.insert(0, declaringClass.getSimpleName() + ".");
            declaringClass = declaringClass.getDeclaringClass();
        }
        return className.toString();
    }

    public static class FieldValue {
        public Object value;
        public boolean isAccessable;

        public FieldValue(Object value, boolean isAccessable) {
            this.value = value;
            this.isAccessable = isAccessable;
        }
    }

    public static FieldValue getValueOfField(Field field, Object o, Class<?> clazz) {
        if (Modifier.isStatic(field.getModifiers())) {
            return new FieldValue(null, false);
        }
        try {
            return new FieldValue(field.get(o), true);
        } catch (IllegalAccessException ignored) {
        }
        String fieldName = field.getName();
        Set<String> methodNames = new HashSet<>();
        methodNames.add(fieldName);
        methodNames.add("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        if(field.getType() == Boolean.class || field.getType() == boolean.class){
            methodNames.add("is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        }

        Optional<Method> getter = Stream.of(clazz.getDeclaredMethods())
                .filter(method -> methodNames.contains(method.getName()))
                .filter(method -> method.getParameterCount() == 0)
                .findFirst();
        if (!getter.isPresent()) {
            return new FieldValue(null, false);
        }

        try {
            return new FieldValue(getter.get().invoke(o), true);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new FieldValue(null, false);
        }
    }

}
