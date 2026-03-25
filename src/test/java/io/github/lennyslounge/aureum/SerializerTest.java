package io.github.lennyslounge.aureum;

import io.github.lennyslounge.aureum.writer.ReflectionWriter;
import org.junit.jupiter.api.Test;

public class SerializerTest {

    @Test
    public void shouldUseTypeSpecificWriter() {
        GoldenMaster verifier = GoldenMaster.defaultConfig()
                .withWriterForClass(Integer.class, (s, obj) -> "int:" + obj);
        verifier.verify((Object) 42);
    }

    @Test
    public void shouldFallBackToDefaultWriter() {
        GoldenMaster verifier = GoldenMaster.defaultConfig()
                .withWriterForClass(Integer.class, (s, obj) -> "int:" + obj);
        verifier.verify((Object) 99L);
    }


    public static class ParentClass {
        public String msg;

        public ParentClass(String msg) {
            this.msg = msg;
        }
    }

    public static class DerivedClass extends ParentClass {

        public DerivedClass(String msg) {
            super(msg);
        }
    }

    @Test
    public void shouldUseAssignableClassWriter() {
        DerivedClass derived = new DerivedClass("This text should not appear");

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter().withPrettyPrinting())
                .withWriterForSubclassOf(ParentClass.class, (s, obj) -> "This text should appear")
                .verify(derived);
    }

}
