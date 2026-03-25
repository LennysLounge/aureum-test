package io.github.lennyslounge.aureum.writer;

import io.github.lennyslounge.aureum.Serializer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

public class ReflectionWriter implements Writer<Object> {

    private final boolean usePrettyPrinting;
    private final Set<String> ignoreFields;
    private final Map<String, String> replaceWithPlaceholder;
    private final Map<String, String> replaceWithOccurrence;

    private final Map<String, Integer> uniqueOccurrencesPerPlaceholder = new HashMap<>();
    private final Map<String, Integer> numberForOccurrence = new HashMap<>();

    /*
    - ignore fields of type
    - ignore fields that match regex
    - replace with placeholder
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
        this(false, new HashSet<>(), new HashMap<>(), new HashMap<>());
    }

    private ReflectionWriter(
            boolean usePrettyPrinting,
            Set<String> ignoreFields,
            Map<String, String> replaceWithPlaceholder,
            Map<String, String> replaceWithOccurrence
    ) {
        this.usePrettyPrinting = usePrettyPrinting;
        this.ignoreFields = ignoreFields;
        this.replaceWithPlaceholder = replaceWithPlaceholder;
        this.replaceWithOccurrence = replaceWithOccurrence;
    }

    public ReflectionWriter withPrettyPrinting() {
        return new ReflectionWriter(true, ignoreFields, replaceWithPlaceholder, replaceWithOccurrence);
    }

    public ReflectionWriter withIgnoreFields(String... fieldsToIgnore) {
        Set<String> newIgnoreFields = new HashSet<>(this.ignoreFields);
        newIgnoreFields.addAll(Arrays.asList(fieldsToIgnore));
        return new ReflectionWriter(usePrettyPrinting, newIgnoreFields, replaceWithPlaceholder, replaceWithOccurrence);
    }

    public ReflectionWriter withReplaceFieldWithPlaceholder(String fieldName, String placeholder) {
        Map<String, String> newMap = new HashMap<>(this.replaceWithPlaceholder);
        newMap.put(fieldName, placeholder);
        return new ReflectionWriter(usePrettyPrinting, ignoreFields, newMap, replaceWithOccurrence);
    }

    public ReflectionWriter withReplaceFieldWithOccurrence(String fieldName, String placeholder) {
        Map<String, String> newMap = new HashMap<>(this.replaceWithOccurrence);
        newMap.put(fieldName, placeholder);
        return new ReflectionWriter(usePrettyPrinting, ignoreFields, replaceWithPlaceholder, newMap);
    }

    @Override
    public String apply(Serializer serializer, Object o) {
        Class<?> clazz = o.getClass();

        StringBuilder sb = new StringBuilder();
        sb.append(getClassName(clazz))
                .append("[");

        serializer.increaseIndent();
        boolean first = true;
        for (Field field : getAllFieldsInInheritanceChain(clazz)) {
            FieldValue value = getField(serializer, field, o, clazz);
            if (value.isAccessible) {
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
                if (value.value instanceof ReplacedValue) {
                    sb.append(field.getName())
                            .append(": ")
                            .append("<")
                            .append(((ReplacedValue) value.value).replacedValue)
                            .append(">");
                } else {
                    sb.append(field.getName())
                            .append(": ")
                            .append(serializer.toString(value.value));
                }

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

    public static List<Field> getAllFieldsInInheritanceChain(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            fields.addAll(0, Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        }
        return fields;
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

    private static class ReplacedValue {
        String replacedValue;

        public ReplacedValue(String replacedValue) {
            this.replacedValue = replacedValue;
        }
    }

    public static class FieldValue {
        public Object value;
        public boolean isAccessible;

        public static FieldValue notAccessible() {
            FieldValue result = new FieldValue();
            result.isAccessible = false;
            return result;
        }

        public static FieldValue value(Object value) {
            FieldValue result = new FieldValue();
            result.isAccessible = true;
            result.value = value;
            return result;
        }
    }

    private FieldValue getField(Serializer serializer, Field field, Object o, Class<?> clazz) {
        if (ignoreFields.contains(field.getName())) {
            return FieldValue.notAccessible();
        }
        String placeholder = replaceWithPlaceholder.get(field.getName());
        if (placeholder != null) {
            return FieldValue.value(new ReplacedValue(placeholder));
        }
        String occurrencePlaceholder = replaceWithOccurrence.get(field.getName());
        if (occurrencePlaceholder != null) {
            FieldValue fieldValue = getValueOfField(field, o, clazz);
            if (fieldValue.isAccessible) {
                String fieldAsString = serializer.toString(fieldValue.value);
                Integer number = numberForOccurrence.get(fieldAsString);
                if (number == null) {
                    number = uniqueOccurrencesPerPlaceholder.merge(occurrencePlaceholder, 1, Integer::sum);
                    numberForOccurrence.put(fieldAsString, number);
                }
                return FieldValue.value(new ReplacedValue(occurrencePlaceholder + "_" + number));
            } else {
                return fieldValue;
            }
        }
        return getValueOfField(field, o, clazz);
    }

    public static FieldValue getValueOfField(Field field, Object o, Class<?> clazz) {
        if (Modifier.isStatic(field.getModifiers())) {
            return FieldValue.notAccessible();
        }
        try {
            return FieldValue.value(field.get(o));
        } catch (IllegalAccessException ignored) {
        }
        String fieldName = field.getName();
        Set<String> methodNames = new HashSet<>();
        methodNames.add(fieldName);
        methodNames.add("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
            methodNames.add("is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
        }

        Optional<Method> getter = Stream.of(clazz.getDeclaredMethods())
                .filter(method -> methodNames.contains(method.getName()))
                .filter(method -> method.getParameterCount() == 0)
                .findFirst();
        if (!getter.isPresent()) {
            return FieldValue.notAccessible();
        }

        try {
            return FieldValue.value(getter.get().invoke(o));
        } catch (IllegalAccessException | InvocationTargetException e) {
            return FieldValue.notAccessible();
        }
    }

}
