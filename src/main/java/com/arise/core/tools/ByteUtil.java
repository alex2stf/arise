package com.arise.core.tools;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;

public class ByteUtil {

    public static ByteBuffer concat(List<ByteBuffer> buffers) {
        int length = 0;
        for (ByteBuffer bb : buffers) {
            bb.rewind();
            length += bb.remaining();
        }
        ByteBuffer bbNew = ByteBuffer.allocate(length);

        for (ByteBuffer bb : buffers) {
            bb.rewind();
            bbNew.put(bb.array());

        }
        bbNew.rewind();
        return bbNew;
    }

    public static ByteBuffer clone(final ByteBuffer original) {
        // Create clone with same capacity as original.
        final ByteBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity()) :
                ByteBuffer.allocate(original.capacity());

        // Create a read-only copy of the original.
        // This allows reading from the original without modifying it.
        final ByteBuffer readOnlyCopy = original.asReadOnlyBuffer();

        // Flip and read from the original.
        readOnlyCopy.flip();
        clone.put(readOnlyCopy);

        return clone;
    }


    /**
     * Get a UTF-8 byte array representation of the given string.
     */
    public static byte[] getBytesUTF8(String string)
    {
        if (string == null)
        {
            return null;
        }

        try
        {
            return string.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This never happens.
            return null;
        }
    }


    /**
     * Convert a UTF-8 byte array into a string.
     */
    public static String toStringUTF8(byte[] bytes)
    {
        if (bytes == null) {
            return null;
        }

        return toStringUTF8(bytes, 0, bytes.length);
    }


    /**
     * Convert a UTF-8 byte array into a string.
     */
    public static String toStringUTF8(byte[] bytes, int offset, int length)
    {
        if (bytes == null) {
            return null;
        }

        try {
            return new String(bytes, offset, length, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // This never happens.
            return null;
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }


    private static final SecureRandom sRandom = new SecureRandom();
    /**
     * Fill the given buffer with random bytes.
     */
    public static byte[] randBytes(byte[] buffer) {
        sRandom.nextBytes(buffer);
        return buffer;
    }


    /**
     * Create a buffer of the given size filled with random bytes.
     */
    public static byte[] randBytes(int nBytes) {
        byte[] buffer = new byte[nBytes];

        return randBytes(buffer);
    }
}
