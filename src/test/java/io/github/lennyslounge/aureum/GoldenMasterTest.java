package io.github.lennyslounge.aureum;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GoldenMasterTest {

    @Test
    public void shouldNotThrow(){
        GoldenMaster.verify("Hello World!");
    }

    @Test
    public void shouldThrow(){
        assertThatThrownBy(() -> GoldenMaster.verify("Hello World"))
                .isInstanceOf(AssertionFailedError.class);

        Path receivedFile = Paths.get("src/test/java/io/github/lennyslounge/aureum/GoldenMasterTest.shouldThrow.received.txt");
        assertThat(receivedFile).exists();

        receivedFile.toFile().delete();
    }
}
