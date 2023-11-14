package physics;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;
public class PhysicisTransmitter implements Runnable{
    private final LinkedBlockingQueue<float []> txAudioBuffer;
    private final LinkedBlockingQueue<byte []> txDataBuffer;
    private final int bitWidth;
    private final Codec codec;
    private final int bufferSize;
    public PhysicisTransmitter(LinkedBlockingQueue<float[]> txAudioBuffer,
                               LinkedBlockingQueue<byte[]> txDataBuffer,
                               int bitWidth,
                               Codec codec,
                               int bufferSize) {
        this.txAudioBuffer = txAudioBuffer;
        this.txDataBuffer = txDataBuffer;
        this.bitWidth = bitWidth;
        this.codec = codec;
        this.bufferSize = bufferSize;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (!Thread.currentThread().isInterrupted()) {
            if (bufferSize == 0) {
                continue;
            }

            try {
                byte[] data = txDataBuffer.take();
                byte[] frame = Arrays.copyOf(data, data.length);
                float[] modulated = codec.modulate(frame, bitWidth);
                int chunkNums = (int) Math.ceil((double) modulated.length / bufferSize);
                for (int i = 0; i < chunkNums - 1; i++) {
                    float[] chunk = Arrays.copyOfRange(modulated, i * bufferSize, (i + 1) * bufferSize);
                    txAudioBuffer.put(chunk);
                }
                float[] chunk = new float[bufferSize];
                for (int i = (chunkNums - 1) * bufferSize; i < chunkNums * bufferSize; i++) {
                    int chunkIndex = i - (chunkNums - 1) * bufferSize;
                    if (i < modulated.length) {
                        chunk[chunkIndex] = modulated[i];
                    } else {
                        chunk[chunkIndex] = 0;
                    }
                }
                txAudioBuffer.put(chunk);
                float[] paddingChunk = new float[bufferSize];
                Arrays.fill(paddingChunk, 0);
                txAudioBuffer.put(paddingChunk);
                txAudioBuffer.put(paddingChunk);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
