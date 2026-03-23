package io.github.lennyslounge.aureum.ReflectionWriterTest;

import io.github.lennyslounge.aureum.GoldenMasters;
import io.github.lennyslounge.aureum.GoldenMaster;
import io.github.lennyslounge.aureum.ReflectionWriter;
import org.junit.jupiter.api.Test;

public class ReflectionWriterTest {

    GoldenMaster master = GoldenMaster.defaultConfig()
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
