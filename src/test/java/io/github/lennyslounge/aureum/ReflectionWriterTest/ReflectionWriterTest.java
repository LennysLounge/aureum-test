package io.github.lennyslounge.aureum.ReflectionWriterTest;

import io.github.lennyslounge.aureum.GoldenMaster;
import io.github.lennyslounge.aureum.SerializerTest;
import io.github.lennyslounge.aureum.writer.ReflectionWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
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

    public static class Foo {
        public String uuid;
        public String msg;

        public Foo(String uuid, String msg) {
            this.uuid = uuid;
            this.msg = msg;
        }
    }

    @Test
    public void replaceWithOccurrenceCount() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String uuid3 = UUID.randomUUID().toString();

        List<Foo> foos = Arrays.asList(
                new Foo(uuid1, "first uuid"),
                new Foo(uuid2, "second uuid"),
                new Foo(uuid1, "this is the same as the first one"),
                new Foo(uuid3, "third uuid"),
                new Foo(uuid3, "this is the same as the third"),
                new Foo(uuid2, "this is the same as the second"),
                new Foo(uuid2, "this is the same as the second"),
                new Foo(uuid2, "this is the same as the second")
        );

        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter()
                        .withReplaceFieldWithOccurrence("uuid", "UUID")
                )
                .verify(foos);
    }
}
