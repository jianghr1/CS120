package physics.base;

import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class BaseAudioPhysics {
    private final LinkedBlockingQueue<float[]> audioTxBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<float[]> audioRxBuffer = new LinkedBlockingQueue<>();
    private final AsioDriver asioDriver;
    private boolean started = false;

    public BaseAudioPhysics() {
        List<String> driverNameList = AsioDriver.getDriverNames();
        asioDriver = AsioDriver.getDriver(driverNameList.get(0));
//        asioDriver.setSampleRate(48000.0);
        this.init();
    }

    public BaseAudioPhysics(int driverIdx) {
        List<String> driverNameList = AsioDriver.getDriverNames();
        asioDriver = AsioDriver.getDriver(driverNameList.get(driverIdx));
//        asioDriver.setSampleRate(48000.0);
        this.init();
    }

    public BaseAudioPhysics(String driverName) {
        asioDriver = AsioDriver.getDriver(driverName);
    }

    private void init() {
        asioDriver.openControlPanel();
        int bufferSize = getBufferSize();

        Listener listener = new Listener(audioTxBuffer, audioRxBuffer, bufferSize);

        asioDriver.addAsioDriverListener(listener);

        Set<AsioChannel> activeChannels = new HashSet<>();

        activeChannels.add(asioDriver.getChannelInput(0));
        activeChannels.add(asioDriver.getChannelOutput(0));

        asioDriver.createBuffers(activeChannels);
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

    public float[] poll() {
        return this.audioRxBuffer.poll();
    }

    public void put(float[] waves) throws InterruptedException {
        this.audioTxBuffer.put(waves);
    }

    public void clearRxBuffer() {
        this.audioRxBuffer.clear();
    }


    public void playAndRecord(ArrayList<float[]> waves) throws InterruptedException {
        this.clearRxBuffer();
        /* play 10s tones and record */
        for (float[] carrier : waves) {
            this.put(carrier);
        }
        /* start to play & record */
        this.start();
        /* wait 10 seconds */
        Thread.sleep(10 * 1000);
        /* stop playing */
        this.stop();
    }

    public ArrayList<float[]> tranferRecordToPlay() throws InterruptedException {
        System.out.println("Transferring recorded waves to Tx buffer");
        ArrayList<float[]> waves = new ArrayList<>();
        float[] capturedWave = this.poll();
        while (capturedWave != null) {
            waves.add(capturedWave);
            this.put(capturedWave);
            capturedWave = this.poll();
        }
        System.out.println("Recorded waves have been transferred to Tx buffer.");
        return waves;
    }

    public void release() {
        this.asioDriver.shutdownAndUnloadDriver();
    }
}
