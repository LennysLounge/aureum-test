package io.github.lennyslounge.aureum;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LineComparatorTest {

    private static GoldenMasterVerifier master = GoldenMaster.defaultVerifier().withComparator(new LineComparator());

    @Test
    public void shouldMatchWithDifferentLineEndings() throws IOException {
        Path approvedFile = master.resolveFileName(FileNamingStrategy.Role.APPROVED);
        Path receivedFile = master.resolveFileName(FileNamingStrategy.Role.RECEIVED);

        Files.createFile(approvedFile);
        Files.write(approvedFile, "first line\r\nsecond line\nthird line".getBytes());

        master.verify("first line\nsecond line\r\nthird line\n");

        Files.delete(approvedFile);
        assertThat(Files.exists(receivedFile)).isFalse();
    }

    @Test
    public void shouldMatchIgnoringTrailingWhitespace() throws IOException {
        Path approvedFile = master.resolveFileName(FileNamingStrategy.Role.APPROVED);
        Path receivedFile = master.resolveFileName(FileNamingStrategy.Role.RECEIVED);

        Files.createFile(approvedFile);
        Files.write(approvedFile, ("This line has trailing whitespace in the master but not in the received file    \n"
                + "This line has trailing whitespace in the received file but not in the master")
                .getBytes(StandardCharsets.UTF_8));

        String receivedText = "This line has trailing whitespace in the master but not in the received file\n"
                + "This line has trailing whitespace in the received file but not in the master    ";

        assertThatThrownBy(() -> GoldenMaster.verify(receivedText))
                 .isInstanceOf(AssertionFailedError.class);

        Files.delete(receivedFile);

        master.withComparator(new LineComparator().ignoreTrailingWhitespace())
                 .verify(receivedText);

        Files.delete(approvedFile);
    }

}
