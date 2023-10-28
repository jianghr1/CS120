package physics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface Codec {

    public abstract float[] modulate(byte[] frame, int bitWidth);

    public abstract float[] getPreamble();

    public abstract byte[] demodulate(float[] data, int bitWidth);
}
