package io.github.lennyslounge;

public class GoldenMaster {

    private static final GoldenMasterVerifier DEFAULT_VERIFIER = new GoldenMasterVerifier();

    public static GoldenMasterVerifier defaultVerifier() {
        return DEFAULT_VERIFIER;
    }

    public static void verify(String actual) {
        DEFAULT_VERIFIER.verify(actual);
    }
}
 