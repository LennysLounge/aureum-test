package io.github.lennyslounge.aureum;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GoldenMasterTest {

    @Test
    public void shouldNotThrow(){
        GoldenMaster.verify("Hello World!");
    }

    @Test
    public void shouldThrow(){
        assertThatThrownBy(() -> {
            GoldenMaster.verify("Hello World");
        }).isInstanceOf(AssertionFailedError.class);
    }
}
