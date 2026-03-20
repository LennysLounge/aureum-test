package io.github.lennyslounge.aureum;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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


    static class Foo{
        String name;
        int age;

        public Foo(String name, int age){
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void serializeList(){
        List<Foo> l = new ArrayList<>();
        l.add(new Foo("alice", 21));
        l.add(new Foo("bob", 71));


        GoldenMaster.defaultVerifier()
                .withWriterForClass(Foo.class, (s, f) -> String.format("Foo: [name: %s, age: %d]", f.name, f.age))
                .verify(l);
        //GoldenMaster.verify(l);
    }
}
