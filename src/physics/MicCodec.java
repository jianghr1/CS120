package physics;

import physics.base.CarrierGenerator;

public class MicCodec extends Codec {
    MicCodec(double sampleRate, int bufferSize, int bitWidth, int frameSize) {
        CarrierGenerator carrierGenerator = new CarrierGenerator(sampleRate, bufferSize);
        carrier = carrierGenerator.genCarrierArray(6000, "sin", 8 * bitWidth * (frameSize + 2));
        preamble = carrierGenerator.genCarrierArray(12000, "sin", 8 * bitWidth * 2);
        for (int i = preamble.length / 2; i < preamble.length; i++) {
            preamble[i] = preamble[preamble.length - i];
        }
        System.out.println(carrier.length);
    }

}
