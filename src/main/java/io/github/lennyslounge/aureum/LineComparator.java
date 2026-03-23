package io.github.lennyslounge.aureum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LineComparator implements Comparator {

    private final boolean ignoreTrailingWhitespace;

    public LineComparator() {
        this(false);
    }

    private LineComparator(boolean ignoreWhitespace) {
        this.ignoreTrailingWhitespace = ignoreWhitespace;
    }

    public LineComparator ignoreTrailingWhitespace() {
        return new LineComparator(true);
    }

    @Override
    public boolean isEqual(InputStream approved, InputStream received) {
        // Wrap streams in Readers to handle UTF-8 decoding and line breaking safely
        try (BufferedReader approvedReader = new BufferedReader(new InputStreamReader(approved, StandardCharsets.UTF_8));
                 BufferedReader receivedReader = new BufferedReader(new InputStreamReader(received, StandardCharsets.UTF_8))) {

            String approvedLine;
            String receivedLine;

            while (true) {
                approvedLine = approvedReader.readLine();
                receivedLine = receivedReader.readLine();

                // If both reached the end of the file simultaneously, it's a match
                if (approvedLine == null && receivedLine == null) {
                    return true;
                }

                // If one file is longer than the other, it's a mismatch
                if (approvedLine == null || receivedLine == null){
                    return false;
                }

                if(ignoreTrailingWhitespace) {
                    approvedLine = approvedLine.trim();
                    receivedLine = receivedLine.trim();
                }

                // Compare the normalized lines
                if (!approvedLine.equals(receivedLine)) {
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
