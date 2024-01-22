package physics;

import physics.base.CarrierGenerator;

public class MicOFDMCodec extends Codec {
    final float[] carrier1;
    final float[] carrier2;
    MicOFDMCodec(double sampleRate, int bufferSize, int bitWidth, int frameSize) {
        CarrierGenerator carrierGenerator = new CarrierGenerator(sampleRate, bufferSize);
//        carrier = carrierGenerator.genCarrierArray(2000, "sin", 16 * bitWidth * (frameSize));
        carrier1 = carrierGenerator.genCarrierArray(4000, "sin", 16*bitWidth*frameSize);
        carrier2 = carrierGenerator.genCarrierArray(8000, "cos", 16*bitWidth*frameSize);
        preamble = carrierGenerator.genPrembleArray(128);
//        System.out.println(carrier.length);
    }

    @Override
    public float[] modulateBit(boolean[] frame, int bitWidth) {
        // bitwidth is the # of samples per bit after OFDM!!!
        float[] wave = new float[preamble.length + frame.length * bitWidth];
        assert(frame.length % 2 == 0);

        int waveIndex = 0;
        for (float p : preamble) {
            wave[waveIndex++] = p;
        }

        for (int i = 0; i < frame.length; i+=2) {
            for (int j = 0; j < bitWidth * 2; j++) {
                wave[waveIndex] = 0;
                if (frame[i+0])
                    wave[waveIndex] += carrier1[waveIndex - preamble.length];
                else
                    wave[waveIndex] -= carrier1[waveIndex - preamble.length];
                if (frame[i+1])
                    wave[waveIndex] += carrier2[waveIndex - preamble.length];
                else
                    wave[waveIndex] -= carrier2[waveIndex - preamble.length];
                wave[waveIndex] /= 2;
                waveIndex++;
            }
        }

        return wave;
    }

    @Override
    public float[] modulateByte(byte[] frame, int bitWidth) {
        return modulateBit(utils.bytesToBits(frame), bitWidth);
    }

    @Override
    public boolean[] demodulateBit(float[] data, int bitWidth) {
        float[] corr1 = new float[data.length];
        float[] corr2 = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            corr1[i] = data[i] * carrier1[i];
            corr2[i] = data[i] * carrier2[i];
        }

        boolean[] frame = new boolean[data.length / bitWidth];
        assert(frame.length % 2 == 0);
        for (int i = 0; i < frame.length; i+=2) {
            float power1=0;
            float power2=0;
            for (int k = 0; k < bitWidth * 2; k++) {
                power1 += corr1[bitWidth * i + k];
                power2 += corr2[bitWidth * i + k];
            }
            frame[i + 0] = power1 > 0;
            frame[i + 1] = power2 > 0;
        }
        return frame;
    }

    @Override
    public byte[] demodulateByte(float[] data, int bitWidth) {
        return utils.bitsToBytes(demodulateBit(data, bitWidth));
    }
}
