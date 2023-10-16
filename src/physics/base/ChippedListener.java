package physics.base;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriverListener;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ChippedListener implements  AsioDriverListener{
    private final LinkedBlockingQueue<int[]> txBuffer;
    private final LinkedBlockingQueue<int[]> rxBuffer;
    private int bufferSize;
    private int[] emptyBuffer;

    private  void assignEmptyBuffer() {
        this.emptyBuffer = new int[this.bufferSize];
        Arrays.fill(this.emptyBuffer, 0);
    }

    public ChippedListener(LinkedBlockingQueue<int[]> txBuffer, LinkedBlockingQueue<int[]> rxBuffer, int bufferSize) {
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
                int[] offered = new int[this.bufferSize];
                for (int i = 0; i < this.bufferSize; i++) {
                    offered[i] = (int)(input[i] * 32768);
                }
                this.rxBuffer.offer(offered);
            } else {
                int[] polled = this.txBuffer.poll();
                polled = (polled == null) ? this.emptyBuffer : polled;
                float[] output = new float[this.bufferSize];
                for (int i = 0; i < this.bufferSize; i++) {
                    output[i] = (float)polled[i] / 32768;
                }
                channel.write(output);
            }
        }
    }
}
