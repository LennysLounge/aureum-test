package io.github.lennyslounge.aureum;

@FunctionalInterface
public interface Serializer {
    String serialize(Object obj);
}
