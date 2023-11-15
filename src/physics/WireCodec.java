package physics;
import physics.base.CarrierGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WireCodec extends Codec {
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
    }

}
