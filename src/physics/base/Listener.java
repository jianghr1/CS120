package physics.base;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriverListener;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class Listener implements  AsioDriverListener{
    private final LinkedBlockingQueue<float[]> txBuffer;
    private final LinkedBlockingQueue<float[]> rxBuffer;
    private Integer bufferSize;
    private float[] emptyBuffer;

    private  void assignEmptyBuffer() {
        this.emptyBuffer = new float[this.bufferSize];
        Arrays.fill(this.emptyBuffer, 0);
    }

    public Listener(LinkedBlockingQueue<float[]> txBuffer, LinkedBlockingQueue<float[]> rxBuffer, Integer bufferSize) {
        this.txBuffer = txBuffer;
        this.rxBuffer = rxBuffer;
        this.bufferSize = bufferSize;
    }

    @Override
    public void sampleRateDidChange(double sampleRate) {
        System.out.println("=== Changed Sample Rate to "+ sampleRate + " Hz ===");
    }

    @Override
    public void resetRequest() {
        System.out.println("=== Reset Requested ===");
    }

    @Override
    public void resyncRequest() {
        System.out.println("=== Resync Requested ===");
    }

    @Override
    public void bufferSizeChanged(int bufferSize) {
        System.out.println("=== Changing Buffer Size to "+ bufferSize + " samples ===");
        this.bufferSize = bufferSize;
        this.assignEmptyBuffer();
        System.out.println("=== Changed Buffer Size to "+ bufferSize + " samples ===");
    }

    @Override
    public void latenciesChanged(int inputLatency, int outputLatency) {
        System.out.println("=== Changed Latency of input to "+ inputLatency + "; output to " + outputLatency + " ===");
    }

    @Override
    public void bufferSwitch(long sampleTime, long samplePosition, Set<AsioChannel> activeChannels) {
        for (AsioChannel channel : activeChannels){
            if (channel.isInput()) {
                float[] input = new float[this.bufferSize];
                channel.read(input);
                this.rxBuffer.offer(input);
            } else {
                float[] output = this.txBuffer.poll();
                output = (output == null) ? this.emptyBuffer : output;
                channel.write(output);
            }
        }
    }
}
