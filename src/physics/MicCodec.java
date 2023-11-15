package physics;

import physics.base.CarrierGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class MicCodec extends Codec {
    MicCodec(double sampleRate, int bufferSize, int bitWidth, int frameSize) {
        CarrierGenerator carrierGenerator = new CarrierGenerator(sampleRate, bufferSize);
        carrier = carrierGenerator.genCarrierArray((int) 6000,"sin",8*bitWidth*(frameSize+2));
        preamble = carrierGenerator.genCarrierArray((int) 12000,"sin",8*bitWidth*2);
        for (int i = preamble.length/2; i < preamble.length; i++) {
            preamble[i] = preamble[preamble.length-i];
        }
        System.out.println(carrier.length);
    }

}
