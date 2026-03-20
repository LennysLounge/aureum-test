package io.github.lennyslounge.aureum;

import java.util.function.BiFunction;

@FunctionalInterface
public interface Writer<T> extends BiFunction<Serializer, T, String> {

}
