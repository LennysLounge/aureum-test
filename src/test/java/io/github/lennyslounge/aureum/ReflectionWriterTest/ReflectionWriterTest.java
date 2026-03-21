package io.github.lennyslounge.aureum.ReflectionWriterTest;

import io.github.lennyslounge.aureum.GoldenMaster;
import io.github.lennyslounge.aureum.GoldenMasterVerifier;
import io.github.lennyslounge.aureum.ReflectionWriter;
import org.junit.jupiter.api.Test;

public class ReflectionWriterTest {

    GoldenMasterVerifier master = GoldenMaster.defaultVerifier()
            .withFallbackWriter(new ReflectionWriter().withPrettyPrinting());

    @Test
    public void testFieldVisibility(){
        ClassWithDifferentFeatures c = new ClassWithDifferentFeatures();

        master.verify(c);
    }


    public static class InnerClass{
        public int publicField = 12;
        private String privateString = "ok";

        public String getPrivateString(){
            return privateString;
        }
    }

    @Test
    public void testInnerClass(){
        InnerClass ic = new InnerClass();

        master.verify(ic);
    }
}
