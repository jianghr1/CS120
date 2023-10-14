package physics;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import com.synthbot.jasiohost.AsioDriver;
import com.synthbot.jasiohost.AsioChannel;
import com.synthbot.jasiohost.AsioDriverInfo;
public class Physicis {
    private final LinkedBlockingQueue<float []> audioTxBuffer = new LinkedBlockingQueue<float[]>();
    private final LinkedBlockingQueue<float []> audioRxBuffer = new LinkedBlockingQueue<float[]>();
    private boolean started = false;
    private AsioDriver asioDriver;

    public Physicis() {
        List<String> driverNameList = AsioDriver.getDriverNames();
        asioDriver = AsioDriver.getDriver(driverNameList.get(0));
    }

    public int getBufferSize() {
        return asioDriver.getBufferPreferredSize();
    }

    public double getSampleRate() {
        return asioDriver.getSampleRate();
    }

    public void start() {

    }

}
