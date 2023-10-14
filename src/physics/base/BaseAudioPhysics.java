package physics.base;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class BaseAudioPhysics {
    private final LinkedBlockingQueue<float[]> audioTxBuffer = new LinkedBlockingQueue<float[]>();
    private final LinkedBlockingQueue<float[]> audioRxBuffer = new LinkedBlockingQueue<float[]>();
    private final AsioDriver asioDriver;
    private boolean started = false;

    public BaseAudioPhysics() {
        List<String> driverNameList = AsioDriver.getDriverNames();
        asioDriver = AsioDriver.getDriver(driverNameList.get(0));
        this.init();
    }

    public BaseAudioPhysics(int driverIdx) {
        List<String> driverNameList = AsioDriver.getDriverNames();
        asioDriver = AsioDriver.getDriver(driverNameList.get(driverIdx));
        this.init();
    }

    private void init() {
        int bufferSize = getBufferSize();

        Listener listener = new Listener(audioTxBuffer, audioRxBuffer, bufferSize);

        asioDriver.addAsioDriverListener(listener);

        Set<AsioChannel> activeChannels = new HashSet<>();

        activeChannels.add(asioDriver.getChannelInput(0));
        activeChannels.add(asioDriver.getChannelOutput(0));

        asioDriver.createBuffers(activeChannels);
    }

    public BaseAudioPhysics(String driverName) {
        asioDriver = AsioDriver.getDriver(driverName);
    }

    public int getBufferSize() {
        return asioDriver.getBufferPreferredSize();
    }

    public double getSampleRate() {
        return asioDriver.getSampleRate();
    }

    public void start() {
        if (this.started)
            return;

        asioDriver.start();

        this.started = true;
    }

    public void stop() {
        if (!this.started)
            return;

        asioDriver.stop();

        this.started = false;
    }

    public float[] take() throws InterruptedException {
        return this.audioRxBuffer.take();
    }

    public float[] poll() throws InterruptedException {
        return this.audioRxBuffer.poll();
    }

    public void put(float[] waves) throws InterruptedException {
        this.audioTxBuffer.put(waves);
    }

    public void clearRxBuffer() throws InterruptedException {
        this.audioRxBuffer.clear();
    }
}
