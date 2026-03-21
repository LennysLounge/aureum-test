package io.github.lennyslounge.aureum;

import org.junit.jupiter.api.Test;

public class SerializerTest {

    @Test
    public void verifyObjectUsesDefaultSerializer() {
        GoldenMaster.verify((Object) 42);
    }

    @Test
    public void verifyObjectUsesTypeSpecificSerializer() {
        GoldenMasterVerifier verifier = GoldenMaster.defaultVerifier()
                .withWriterForClass(Integer.class, (s, obj) -> "int:" + obj);
        verifier.verify((Object) 42);
    }

    @Test
    public void verifyObjectFallsBackToDefaultSerializer() {
        GoldenMasterVerifier verifier = GoldenMaster.defaultVerifier()
                .withWriterForClass(Integer.class, (s, obj) -> "int:" + obj);
        verifier.verify((Object) 99L);
    }

}
