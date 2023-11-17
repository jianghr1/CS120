package mac;

import physics.Physicis;

import java.util.concurrent.LinkedBlockingQueue;

public class MacTransmitter implements Runnable {
    private final Physicis phy;
    private final LinkedBlockingQueue<MacFrame> txFrameBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Boolean> txResultBuffer = new LinkedBlockingQueue<>();
    private final int addr;
    private final long ACKTimeout;
    private final int maxRetransmitTimes;
    private boolean started = false;

    private final Thread macThread;

    MacTransmitter(Physicis phy, int addr, long ACKTimeout, int maxRetransmitTimes) {
        this.phy = phy;
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
                MacFrame txFrame = txFrameBuffer.poll();
                if (txFrame != null) {
                    System.out.println("transmitting"+ txFrameIdx);
                    txFrame.idx = txFrameIdx % 8;
                    // CSMA here
                    byte[] txData = txFrame.toBytes();
                    phy.write(txData);
                    if (txFrame.requireACK()) {
                        boolean success = false;
                        for (int i = 0; i < maxRetransmitTimes; i++) {
                            byte[] data = phy.read(ACKTimeout);
                            if (data != null) {
                                MacFrame frame = MacFrame.parseFrame(data);
                                if (frame.dest == addr) {
                                    if (frame.type == MacFrameType.ACK) {
                                        success = true;
                                        break;
                                    }
                                }
                            }
                            System.out.println("transmitting"+ txFrameIdx +", Retry: " + (i+1));
                            if (i != maxRetransmitTimes - 1) {
                                // Retransmit
                                phy.write(txData);
                            }
                        }
                        txResultBuffer.put(success);
                        System.out.println("transmitted"+ txFrameIdx +", Success: "+success);
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

