package io.github.lennyslounge.aureum;

import java.io.InputStream;

public interface Comparator {
    boolean isEqual(InputStream approved, InputStream received);
}
