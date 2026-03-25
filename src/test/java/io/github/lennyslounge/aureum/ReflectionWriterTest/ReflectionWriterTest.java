package io.github.lennyslounge.aureum.ReflectionWriterTest;

import io.github.lennyslounge.aureum.GoldenMaster;
import io.github.lennyslounge.aureum.SerializerTest;
import io.github.lennyslounge.aureum.writer.ReflectionWriter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ReflectionWriterTest {

    @Test
    public void fieldVisibility() {
        ClassWithDifferentFeatures c = new ClassWithDifferentFeatures();

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter().withPrettyPrinting())
                .verify(c);
    }


    public static class InnerClass {
        public int publicField = 12;
        private String privateString = "ok";

        public String getPrivateString() {
            return privateString;
        }
    }

    @Test
    public void innerClass() {
        InnerClass ic = new InnerClass();

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter().withPrettyPrinting())
                .verify(ic);
    }

    public static class IgnoreFields {
        public String uuid;
        public String msg;

        public IgnoreFields(String uuid, String msg) {
            this.uuid = uuid;
            this.msg = msg;
        }
    }

    public static class ParentClass {
        public String parentField = "parent class field";
    }

    public static class DerivedClass extends ParentClass {
        public String derivedField = "derived class field";
    }

    @Test
    public void fieldsFromParentClass() {
        DerivedClass derived = new DerivedClass();

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter().withPrettyPrinting())
                .verify(derived);
    }

    @Test
    public void ignoreFields() {
        IgnoreFields obj = new IgnoreFields(
                UUID.randomUUID().toString(),
                "This should not contain the uuid field");

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter()
                        .withPrettyPrinting()
                        .withIgnoreFields("uuid"))
                .verify(obj);

    }

    @Test
    public void replaceFieldWithPlaceholder() {
        IgnoreFields obj = new IgnoreFields(
                UUID.randomUUID().toString(),
                "The uuid field should be there but its value should be replaced with <UUID>");

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter()
                        .withPrettyPrinting()
                        .withReplaceFieldWithPlaceholder("uuid", "UUID"))
                .verify(obj);
    }
}
