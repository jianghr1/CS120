package physics;

import physics.base.CarrierGenerator;

public class MicCodec extends Codec {
    MicCodec(double sampleRate, int bufferSize, int bitWidth, int frameSize) {
        CarrierGenerator carrierGenerator = new CarrierGenerator(sampleRate, bufferSize);
        carrier = carrierGenerator.genCarrierArray(2000, "sin", 16 * bitWidth * (frameSize));
        preamble = carrierGenerator.genPrembleArray(128);
        System.out.println(carrier.length);
    }

}
