package com.wikify.media;

import java.util.Optional;


public final class MediaTypeDetector {


    public static final int HEADER_BYTES = 12;

    private MediaTypeDetector() {
    }

    public static Optional<String> detect(byte[] header) {
        if (starts(header, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) return Optional.of("image/png");
        if (starts(header, 0, 0xFF, 0xD8, 0xFF)) return Optional.of("image/jpeg");
        if (starts(header, 0, 'G', 'I', 'F', '8')) return Optional.of("image/gif");

        if (starts(header, 0, 'R', 'I', 'F', 'F') && starts(header, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of("image/webp");
        }


        if (starts(header, 4, 'f', 't', 'y', 'p')) return Optional.of("video/mp4");

        if (starts(header, 0, 0x1A, 0x45, 0xDF, 0xA3)) return Optional.of("video/webm");

        return Optional.empty();
    }

    private static boolean starts(byte[] bytes, int offset, int... expected) {
        if (bytes.length < offset + expected.length) return false;

        for (int i = 0; i < expected.length; i++) {
            if ((bytes[offset + i] & 0xFF) != (expected[i] & 0xFF)) return false;
        }
        return true;
    }
}
