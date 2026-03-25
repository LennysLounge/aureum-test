package io.github.lennyslounge.aureum.writer;

import io.github.lennyslounge.aureum.Serializer;

import java.util.Objects;

public class ToStringWriter implements Writer<Object> {

    @Override
    public String apply(Serializer serializer, Object o) {
        return Objects.toString(o);
    }
}
