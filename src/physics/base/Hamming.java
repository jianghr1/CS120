package physics.base;

import java.util.Arrays;

public class Hamming {
    public static boolean[] encodeHamming(byte[] raw) {

        boolean[] bits = utils.bytesToBits(raw);

        int numPackages = (int) Math.ceil(bits.length / 120f);

        boolean[] encoded = new boolean[numPackages * 127];

        int rawIdx = 0;

        for (int iPkg = 0; iPkg < numPackages; iPkg++) {

            for (int idx = 0; idx < 127; idx++) {
                if (((idx+1) & idx) != 0) {
                    /* not verifying bit */
                    encoded[iPkg * 127 + idx] = rawIdx < bits.length && bits[rawIdx];

                    for (int t = 0; t < 7; t++) {
                        if (((idx + 1) & (1 << t)) != 0) {
                            encoded[iPkg * 127 + (1 << t) - 1] ^= encoded[iPkg * 127 + idx];
                        }
                    }

                    rawIdx++;
                }
            }
        }
        return encoded;
    }

    public static byte[] decodeHamming(boolean[] encoded) {
        int numPackages = encoded.length / 127;

        boolean[] bits = new boolean[numPackages * 120];

        int rawIdx = 0;
        boolean[] verifyBits = new boolean[7];

        for (int iPkg = 0; iPkg < numPackages; iPkg++) {
            Arrays.fill(verifyBits, false);

            for (int idx = 0; idx < 127; idx++) {
                for (int t = 0; t < 7; t++) {
                    if (((idx + 1) & (1 << t)) != 0) {
                        verifyBits[t] ^= encoded[iPkg * 127 + idx];
                    }
                }
            }

            int errorIdx = 0;

            for (int t = 0; t < 7; t++) {
                errorIdx += verifyBits[t] ? (1 << t) : 0;
            }

            if (errorIdx != 0) {
                System.err.println("[Warn] Detected Err Bit on " + errorIdx + "Bits:" + Arrays.toString(verifyBits));
                encoded[iPkg * 127 + errorIdx - 1] ^= true;
            }

            for (int idx = 0; idx < 127; idx++) {
                if (((idx+1) & idx) != 0) {
                    /* not verifying bit */
                    bits[rawIdx] = encoded[iPkg * 127 + idx];
                    rawIdx++;
                }
            }

        }
        return utils.bitsToBytes(bits);
    }
}
