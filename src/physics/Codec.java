package physics;

import physics.base.Hamming;

public class Codec {

    protected static float[] carrier;
    protected static float[] preamble;

    public boolean isHamming = false;

    public float[] modulate(byte[] frame, int bitWidth) {
        if (isHamming) {
            boolean[] bits = Hamming.encodeHamming(frame);
            return modulateBit(bits, bitWidth);
        } else {
            return modulateByte(frame, bitWidth);
        }
    }

    public float[] modulateBit(boolean[] frame, int bitWidth) {
        int waveLength = preamble.length + frame.length * bitWidth;
        float[] wave = new float[waveLength];

        int waveIndex = 0;
        for (float p : preamble) {
            wave[waveIndex++] = p;
        }

        for (boolean b : frame) {
            if (b) {
                for (int j = 0; j < bitWidth; j++) {
                    wave[waveIndex] = carrier[waveIndex - preamble.length];
                    waveIndex += 1;
                }
            } else {
                for (int j = 0; j < bitWidth; j++) {
                    wave[waveIndex] = -carrier[waveIndex - preamble.length];
                    waveIndex += 1;
                }
            }
        }
        return wave;
    }

    public static float[] modulateByte(byte[] frame, int bitWidth) {

        int waveLength = preamble.length + frame.length * 8 * bitWidth;
        float[] wave = new float[waveLength];

        int waveIndex = 0;
        for (float p : preamble) {
            wave[waveIndex++] = p;
        }

        for (byte b : frame) {
            for (int i = 7; i >= 0; i--) {
                int bit = (b >> i) & 1;
                if (bit == 0) {
                    for (int j = 0; j < bitWidth; j++) {
                        wave[waveIndex] = -carrier[waveIndex - preamble.length];
                        waveIndex += 1;
                    }
                } else {
                    for (int j = 0; j < bitWidth; j++) {
                        wave[waveIndex] = carrier[waveIndex - preamble.length];
                        waveIndex += 1;
                    }
                }
            }
        }
        return wave;
    }

    public float[] getPreamble() {
        return preamble;
    }

    public byte[] demodulate(float[] data, int bitWidth) {
        if (isHamming) {
            return Hamming.decodeHamming(demodulateBit(data, bitWidth));
        } else {
            return demodulateByte(data, bitWidth);
        }
    }

    public boolean[] demodulateBit(float[] data, int bitWidth) {
        float[] corr = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            corr[i] = data[i] * carrier[i];
        }

        boolean[] frame = new boolean[data.length / bitWidth];
        for (int i = 0; i < frame.length; i++) {
            float power = 0;
            for (int k = 1; k < bitWidth - 1; k++) {
                power += corr[bitWidth * i + k];
            }
            frame[i] = power > 0;
        }

        return frame;
    }


    public static byte[] demodulateByte(float[] data, int bitWidth) {
        float[] corr = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            corr[i] = data[i] * carrier[i];
        }

        byte[] frame = new byte[data.length / 8 / bitWidth];
        for (int i = 0; i < frame.length; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++) {
                b = b << 1;
                float power = 0;
                for (int k = 1; k < bitWidth - 1; k++) {
                    power += corr[bitWidth * (8 * i + j) + k];
                }
                if (power > 0) {
                    b += 1;
                }
            }
            frame[i] = (byte) b;
        }

        return frame;
    }
}
