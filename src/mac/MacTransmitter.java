package mac;

import physics.Physicis;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MacTransmitter implements Runnable {
    private final Mac mac;
    private final Physicis phy;
    private final LinkedBlockingQueue<MacFrame> txDispatchFrames;
    private final LinkedBlockingQueue<MacFrame> txFrameBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Boolean> txResultBuffer = new LinkedBlockingQueue<>();
    private final int addr;
    private final long ACKTimeout;
    private final int maxRetransmitTimes;
    private final Thread macThread;
    private boolean started = false;

    MacTransmitter(Mac mac, Physicis phy, LinkedBlockingQueue<MacFrame> txDispatchFrames, int addr, long ACKTimeout, int maxRetransmitTimes) {
        this.mac = mac;
        this.phy = phy;
        this.txDispatchFrames = txDispatchFrames;
        this.addr = addr;
        this.ACKTimeout = ACKTimeout;
        this.maxRetransmitTimes = maxRetransmitTimes;
        macThread = new Thread(this);
    }

    public void start() {
        if (started) {
            return;
        }

        macThread.start();
        phy.start();

        started = true;
    }

    public void stop() {
        if (!started) {
            return;
        }

        macThread.interrupt();
        try {
            macThread.join();
        } catch (InterruptedException ignored) {
        }

        phy.stop();

        started = false;
    }

    public boolean write(MacFrame frame) throws InterruptedException {
        txFrameBuffer.put(frame);
        return txResultBuffer.take();
    }

    @Override
    public void run() {
        int txFrameIdx = 0;
        MacFrame txFrame = null;
        int reTransmitTimes = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (mac.getMacState() != MACState.Tx_waiting) {
                    Thread.sleep(5);
                    continue;
                }
                if (reTransmitTimes == 0) {
                    txFrame = txFrameBuffer.poll();
                }
                if (txFrame != null) {
                    System.out.println("transmitting" + txFrameIdx + "from" + txFrame.src + "to" + txFrame.dest);
                    txFrame.idx = txFrameIdx % 8;
                    byte[] txData = txFrame.toBytes();
                    // CSMA here
                    phy.write(txData);

                    if (txFrame.requireACK()) {
                        boolean success = false;
                        MacFrame frame = txDispatchFrames.poll(ACKTimeout, TimeUnit.MILLISECONDS);
                        if (frame != null) {
                            if (frame.dest == addr) {
                                if (frame.type == MacFrameType.ACK) {
                                    success = true;
                                }
                            }
                        }
                        if (success) {
                            reTransmitTimes = 0;
                            txResultBuffer.put(true);
                            txFrameIdx++;
                        } else {
                            if (reTransmitTimes == maxRetransmitTimes - 1)
                            {
                                reTransmitTimes = 0;
                                txResultBuffer.put(false);
                                txFrameIdx++;
                            } else {
                                ++reTransmitTimes;
                            }
                        }
                    } else {
                        txResultBuffer.put(true);
                        txFrameIdx++;
                    }
                }
                mac.setMacState(MACState.FrameDetection);
                mac.resetTxTimeOutTimeStamp();
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

