package mac;

import physics.Physicis;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class Mac {
    private final Physicis phy;
    private final MacTransmitter macTransmitter;
    private final MacReceiver macReceiver;
    private final int addr;
    private final int frameSize;


    public Mac(int addr, int frameSize, long ACKTimeout, int maxRetransmitTimes) throws IOException {
        this.addr = addr;
        this.frameSize = frameSize;

        phy = new Physicis(4, this.frameSize + 2, "wire", true);

        LinkedBlockingQueue<MacFrame> txDispatchFrames = new LinkedBlockingQueue<>();

        macTransmitter = new MacTransmitter(phy, txDispatchFrames, addr, ACKTimeout, maxRetransmitTimes);

        macReceiver = new MacReceiver(phy, txDispatchFrames, addr, this.frameSize + 2);
    }

    public Mac(int addr) throws IOException {
        this(addr, 50, 500, 5);
    }

    public void start() {
        macTransmitter.start();
        macReceiver.start();
    }

    public void stop() {
        macTransmitter.stop();
        macReceiver.stop();
    }

    public MacFrame read() throws InterruptedException {
        return read(-1);
    }

    public MacFrame readNoWait() throws InterruptedException {
        return read(0);
    }

    public MacFrame read(long timeout) throws InterruptedException {
        return macReceiver.read(timeout);
    }

    public boolean write(MacFrame frame) throws InterruptedException {
        frame.src = addr;
        return macTransmitter.write(frame);
    }

    public boolean write(int dest, byte[] data) throws InterruptedException {
        MacFrame frame = new MacFrame(dest, addr, MacFrameType.DATA, MacFrame.FLAG_PLAIN, 0, data);
        return macTransmitter.write(frame);
    }

    public int getFramePayloadSize() {
        return frameSize;
    }
}