package io.github.lennyslounge.aureum;

public class GoldenMasters {

    public static void verify(Object actual) {
        GoldenMaster.defaultConfig().verify(actual);
    }

    public static void verify(Object actual, String name) {
        GoldenMaster.defaultConfig().verify(actual, name);
    }

    public static void verify(String actual) {
        GoldenMaster.defaultConfig().verify(actual);
    }

    public static void verify(String actual, String name) {
        GoldenMaster.defaultConfig().verify(actual, name);
    }
}
