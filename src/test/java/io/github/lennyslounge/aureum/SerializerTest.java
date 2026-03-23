package io.github.lennyslounge.aureum;

import org.junit.jupiter.api.Test;

public class SerializerTest {

    @Test
    public void verifyObjectUsesDefaultSerializer() {
        GoldenMasters.verify((Object) 42);
    }

    @Test
    public void verifyObjectUsesTypeSpecificSerializer() {
        GoldenMaster verifier = GoldenMaster.defaultConfig()
                .withWriterForClass(Integer.class, (s, obj) -> "int:" + obj);
        verifier.verify((Object) 42);
    }

    @Test
    public void verifyObjectFallsBackToDefaultSerializer() {
        GoldenMaster verifier = GoldenMaster.defaultConfig()
                .withWriterForClass(Integer.class, (s, obj) -> "int:" + obj);
        verifier.verify((Object) 99L);
    }

}
