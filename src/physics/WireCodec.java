package physics;
import physics.base.CarrierGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WireCodec implements Codec{
    private static float[] carrier;
    private static float[] preamble;
    WireCodec(double sampleRate, int bufferSize, int bitWidth, int frameSize) throws IOException {
        try {
            Path path = Paths.get("carrier.txt");
            String carrierString = Files.readString(path);
            String[] carrierStringSplit = carrierString.split(" ");
            carrier = new float[carrierStringSplit.length];
            for (int i = 0; i < carrierStringSplit.length; i++) {
                carrier[i] = Float.parseFloat(carrierStringSplit[i]);
            }
        } catch (IOException e) {
            System.err.println("Fail to load carrier");
            throw new IOException("Fail to load carrier: " + e.getMessage());
        }

        try {
            Path path = Paths.get("preamble.txt");
            String preambleString = Files.readString(path);
            String[] preambleStringSplit = preambleString.split(" ");
            preamble = new float[preambleStringSplit.length];
            for (int i = 0; i < preambleStringSplit.length; i++) {
                preamble[i] = Float.parseFloat(preambleStringSplit[i]);
            }
        } catch (IOException e) {
            System.err.println("Fail to load preamble");
            throw new IOException("Fail to load preamble: " + e.getMessage());
        }
        System.out.println(carrier.length);
        System.out.println(preamble.length);
    }

    @Override
    public float[] modulate(byte[] frame, int bitWidth) {
        int waveLength = preamble.length + frame.length * 8 * bitWidth;
        float[] wave = new float[waveLength];

        int waveIndex = 0;
        for (float p : preamble) {
            wave[waveIndex++] = p;
        }

        System.out.println(frame.length);
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

    @Override
    public float[] getPreamble() {
        return preamble;
    }

    @Override
    public byte[] demodulate(float[] data, int bitWidth) {
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
