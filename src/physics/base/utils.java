package physics.base;

public class utils {
    public static boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];

        int bitIndex = 0;

        for (byte b : bytes) {
            for (int i = 7; i >=0; i--) {
                bits[bitIndex++] = ((b >> i) & 1) == 1;
            }
        }
        return bits;
    }

    public static byte[] bitsToBytes(boolean[] bits) {
        byte[] bytes = new byte[bits.length / 8];

        for (int i = 0; i < bytes.length; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++) {
                b = b << 1;
                b += bits[8 * i + j] ? 1 : 0;
            }
            bytes[i] = (byte)b;
        }

        return bytes;
    }
}

