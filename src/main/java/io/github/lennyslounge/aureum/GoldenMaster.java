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
                      .fileExtension("txt"))
            .withFallbackWriter(new Serializer.ToStringWriter())
            .withCommonWriters()
            .withComparator(new LineComparator());
            ;

    public static GoldenMasterVerifier defaultVerifier() {
        return DEFAULT_VERIFIER;
    }

    public static void verify(Object actual) {
        DEFAULT_VERIFIER.verify(actual);
    }

    public static void verify(Object actual, String name) {
        DEFAULT_VERIFIER.verify(actual, name);
    }
}
