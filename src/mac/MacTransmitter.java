package mac;

import physics.Physicis;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MacTransmitter implements Runnable {
    private final Physicis phy;
    private final LinkedBlockingQueue<MacFrame> txDispatchFrames;
    private final LinkedBlockingQueue<MacFrame> txFrameBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Boolean> txResultBuffer = new LinkedBlockingQueue<>();
    private final int addr;
    private final long ACKTimeout;
    private final int maxRetransmitTimes;
    private final Thread macThread;
    private boolean started = false;

    MacTransmitter(Physicis phy, LinkedBlockingQueue<MacFrame> txDispatchFrames, int addr, long ACKTimeout, int maxRetransmitTimes) {
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
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (MacReceiver.macState != MacReceiver.MACState.Tx) {
                    Thread.sleep(5);
                    continue;
                }
                MacFrame txFrame = txFrameBuffer.poll();
                if (txFrame != null) {
                    System.out.println("transmitting" + txFrameIdx + "from" + txFrame.src + "to" + txFrame.dest);
                    txFrame.idx = txFrameIdx % 8;
                    byte[] txData = txFrame.toBytes();
                    // CSMA here
                    phy.write(txData);

                    if (txFrame.requireACK()) {
                        boolean success = false;
                        for (int i = 0; i < maxRetransmitTimes; i++) {
                            MacFrame frame = txDispatchFrames.poll(ACKTimeout, TimeUnit.MILLISECONDS);
                            if (frame != null) {
                                if (frame.dest == addr) {
                                    if (frame.type == MacFrameType.ACK) {
                                        success = true;
                                        break;
                                    }
                                }
                            }
                            System.out.println("transmitting" + txFrameIdx + ", Retry: " + (i + 1));
                            if (i != maxRetransmitTimes - 1) {
                                // Retransmit
                                phy.write(txData);
                            }
                        }
                        txResultBuffer.put(success);
                        System.out.println("transmitted" + txFrameIdx + ", Success: " + success);
                    } else {
                        txResultBuffer.put(true);
                    }
                    txFrameIdx++;
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

