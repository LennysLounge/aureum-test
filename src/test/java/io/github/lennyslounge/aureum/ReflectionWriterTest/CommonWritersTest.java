package io.github.lennyslounge.aureum.ReflectionWriterTest;

import io.github.lennyslounge.aureum.GoldenMaster;
import io.github.lennyslounge.aureum.writer.ReflectionWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommonWritersTest {

    public static class AllCommonWritersInOne {
        public String string = "String";

        public int primitive_int = 12;
        public Integer boxed_int = 144;

        public boolean primitive_boolean = true;
        public Boolean boxed_boolean = Boolean.TRUE;

        public List<String> list_of_strings = Arrays.asList("one", "two", "three");
        public ArrayList<String> array_list_of_strings = new ArrayList<>(Arrays.asList("one", "two", "three"));
        public LinkedList<String> linked_list_of_strings = new LinkedList<>(Arrays.asList("one", "two", "three"));
    }

    @Test
    public void allWriters() {
        GoldenMaster.defaultConfig()
                .withFallbackWriter(new ReflectionWriter().withPrettyPrinting())
                .verify(new AllCommonWritersInOne());
    }
}
