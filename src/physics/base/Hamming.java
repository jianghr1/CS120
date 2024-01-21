package physics.base;

import physics.utils;

import java.util.Arrays;

public class Hamming {
    public static int hammingOri = 57;
    static public int hammingAdu = 63;
    static public int hammingBit = 6;
    public static boolean[] encodeHamming(byte[] raw) {

        boolean[] bits = utils.bytesToBits(raw);
        System.out.println(bits.length);

        int numPackages = (int) Math.ceil((double) bits.length / hammingOri);

        boolean[] encoded = new boolean[numPackages * hammingAdu];

        int rawIdx = 0;

        for (int iPkg = 0; iPkg < numPackages; iPkg++) {

            for (int idx = 0; idx < hammingAdu; idx++) {
                if (((idx + 1) & idx) != 0) {
                    /* not verifying bit */
                    encoded[iPkg * hammingAdu + idx] = rawIdx < bits.length && bits[rawIdx];

                    for (int t = 0; t < hammingBit; t++) {
                        if (((idx + 1) & (1 << t)) != 0) {
                            encoded[iPkg * hammingAdu + (1 << t) - 1] ^= encoded[iPkg * hammingAdu + idx];
                        }
                    }

                    rawIdx++;
                }
            }
        }
        return encoded;
    }

    public static byte[] decodeHamming(boolean[] encoded) {
        int numPackages = encoded.length / hammingAdu;
        System.out.println(numPackages);

        boolean[] bits = new boolean[numPackages * hammingOri];

        int rawIdx = 0;
        boolean[] verifyBits = new boolean[hammingBit];

        for (int iPkg = 0; iPkg < numPackages; iPkg++) {
            Arrays.fill(verifyBits, false);

            for (int idx = 0; idx < hammingAdu; idx++) {
                for (int t = 0; t < hammingBit; t++) {
                    if (((idx + 1) & (1 << t)) != 0) {
                        verifyBits[t] ^= encoded[iPkg * hammingAdu + idx];
                    }
                }
            }

            int errorIdx = 0;

            for (int t = 0; t < hammingBit; t++) {
                errorIdx += verifyBits[t] ? (1 << t) : 0;
            }

            if (errorIdx != 0) {
                System.err.println("[Warn] Detected Err Bit on " + errorIdx + "Bits:" + Arrays.toString(verifyBits));
                encoded[iPkg * hammingAdu + errorIdx - 1] ^= true;
            }

            for (int idx = 0; idx < hammingAdu; idx++) {
                if (((idx + 1) & idx) != 0) {
                    /* not verifying bit */
                    bits[rawIdx] = encoded[iPkg * hammingAdu + idx];
                    rawIdx++;
                }
            }

        }
        return utils.bitsToBytes(bits);
    }
}
