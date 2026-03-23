package io.github.lennyslounge.aureum;

import java.io.IOException;
import java.io.InputStream;

public class ByteComparator implements Comparator {

    @Override
    public boolean isEqual(InputStream approved, InputStream received) {
        byte[] approvedBuffer = new byte[8192];
        byte[] receivedBuffer = new byte[8192];

        try {
            while (true) {
                int approvedRead = readFully(approved, approvedBuffer);
                int receivedRead = readFully(received, receivedBuffer);

                // If chunk sizes differ, streams are different lengths
                if (approvedRead != receivedRead)
                    return false;

                // If we reached the end of both streams without returning false, they match
                if (approvedRead == 0)
                    return true;

                // Compare the specific bytes read
                for (int i = 0; i < approvedRead; i++) {
                    if (approvedBuffer[i] != receivedBuffer[i]) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to ensure we fill the buffer as much as possible before comparing
    public static int readFully(InputStream is, byte[] buffer) throws IOException {
        int totalRead = 0;
        while (totalRead < buffer.length) {
            int bytesRead = is.read(buffer, totalRead, buffer.length - totalRead);
            if (bytesRead == -1)
                break;
            totalRead += bytesRead;
        }
        return totalRead;
    }
}
