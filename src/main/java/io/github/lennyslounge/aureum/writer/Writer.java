package io.github.lennyslounge.aureum.writer;

import io.github.lennyslounge.aureum.Serializer;

import java.util.function.BiFunction;

@FunctionalInterface
public interface Writer<T> extends BiFunction<Serializer, T, String> {

}
