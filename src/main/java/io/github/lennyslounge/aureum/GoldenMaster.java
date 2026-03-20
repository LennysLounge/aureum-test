package io.github.lennyslounge.aureum;

public class GoldenMaster {

    private static final GoldenMasterVerifier DEFAULT_VERIFIER = new GoldenMasterVerifier()
             .withFileNamingStrategy(new FileNamePattern()
                      .fixed("src/test/java/")
                      .packageAsPath()
                      .className()
                      .methodNameWithPrefix(".")
                      .verificationNameWithPrefixIfPresent(".")
                      .roleWithPrefix(".")
                      .fileExtension("txt"));

    public static GoldenMasterVerifier defaultVerifier() {
        return DEFAULT_VERIFIER;
    }

    public static void verify(String actual) {
        DEFAULT_VERIFIER.verify(actual);
    }

    public static void verify(String actual, String name) {
        DEFAULT_VERIFIER.verify(actual, name);
    }
}
