package physics;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.List;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriverInfo;
import physics.base.Listener;

public class Physicis {
    private final LinkedBlockingQueue<float []> txAudioBuffer = new LinkedBlockingQueue<float[]>();
    private final LinkedBlockingQueue<float []> rxAudioBuffer = new LinkedBlockingQueue<float[]>();
    private final LinkedBlockingQueue<byte []> txDataBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<byte []> rxDataBuffer = new LinkedBlockingQueue<>();
    private boolean started = false;
    private final AsioDriver asioDriver;
    private final Thread rxThread;
    private final Thread txThread;
    private final WireCodec wireCodec;

    public Physicis(int bitWidth, int frameSize) throws IOException {
        List<String> driverNameList = AsioDriver.getDriverNames();
        asioDriver = AsioDriver.getDriver(driverNameList.get(0));

        asioDriver.openControlPanel();
        try {
            asioDriver.setSampleRate(48000f);
        } catch (Exception e) {
            System.err.println("Fail to set sample rate: " + e.getMessage());
        }

        int bufferSize = getBufferSize();
        double sampleRate = getSampleRate();
        wireCodec = new WireCodec(sampleRate, bufferSize, bitWidth, frameSize);

        PhysicisReceiver receiver = new PhysicisReceiver(rxAudioBuffer, rxDataBuffer, bitWidth, wireCodec, frameSize);
        PhysicisTransmitter transmitter = new PhysicisTransmitter(txAudioBuffer, txDataBuffer, bitWidth, wireCodec, getBufferSize());

        rxThread = new Thread(receiver);
        txThread = new Thread(transmitter);
    }

    public int getBufferSize() {
        return asioDriver.getBufferPreferredSize();
    }

    public double getSampleRate() {
        return asioDriver.getSampleRate();
    }

    public void start() {
        if (started) {
            return;
        }

        double sampleRate = getSampleRate();
        int bufferSize = getBufferSize();
        Listener listener = new Listener(rxAudioBuffer, txAudioBuffer, bufferSize);
        asioDriver.addAsioDriverListener(listener);

        Set<AsioChannel> activeChannels = new HashSet<>();
        activeChannels.add(asioDriver.getChannelInput(0));
        activeChannels.add(asioDriver.getChannelOutput(0));
        asioDriver.createBuffers(activeChannels);

        rxThread.start();
        txThread.start();

        asioDriver.start();

        started = true;
    }
    public void stop() {
        if (!started) {
            return;
        }

        rxThread.interrupt();
        txThread.interrupt();
        try {
            rxThread.join();
            txThread.join();
        } catch (InterruptedException ignored) {
        }

        asioDriver.shutdownAndUnloadDriver();

        started = false;
    }

    public byte[] read() throws InterruptedException {
        return read(-1);
    }

    public byte[] readNoWait() throws InterruptedException {
        return read(0);
    }

    public byte[] read(long timeout) throws InterruptedException {
        if (timeout == -1) {
            return rxDataBuffer.take();
        } else if (timeout == 0) {
            return rxDataBuffer.poll();
        } else {
            return rxDataBuffer.poll(timeout, TimeUnit.MILLISECONDS);
        }
    }

    public void write(byte[] data) throws InterruptedException {
        txDataBuffer.put(data);
    }

    public boolean sendComplete() {
        return txAudioBuffer.isEmpty();
    }

}
