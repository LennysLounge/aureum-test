package io.github.lennyslounge.aureum.writer;

import io.github.lennyslounge.aureum.Serializer;

import java.util.List;

public class CommonWriters {

    public static String String(Serializer serializer, String str) {
        return "\"" + str + "\"";
    }

    public static String Integer(Serializer serializer, Integer i) {
        return String.valueOf(i);
    }

    public static String Boolean(Serializer serializer, Boolean b) {
        return String.valueOf(b);
    }

    public static String List(Serializer serializer, List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        serializer.increaseIndent();
        for (Object item : list) {
            if (first) {
                sb.append(System.lineSeparator())
                        .append(serializer.getIndent());
                first = false;
            } else {
                sb.append(",")
                        .append(System.lineSeparator())
                        .append(serializer.getIndent());
            }
            sb.append(serializer.toString(item));
        }
        serializer.decreaseIndent();
        if (!first) {
            sb.append(",")
                    .append(System.lineSeparator())
                    .append(serializer.getIndent());
        }
        sb.append("]");
        return sb.toString();
    }
}
