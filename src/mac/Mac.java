package mac;

import physics.Physicis;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Mac implements Runnable {
    private final Physicis phy;
    private final LinkedBlockingQueue<MacFrame> rxFrameBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<MacFrame> txFrameBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Boolean> txResultBuffer = new LinkedBlockingQueue<>();
    private final int addr;
    private final int frameSize;
    private final Thread macThread;
    private final byte[] ACKData;
    private final long ACKTimeout;
    private final int maxRetransmitTimes;
    private boolean started = false;

    public Mac(int addr, int frameSize, long ACKTimeout, int maxRetransmitTimes) throws IOException {
        this.addr = addr;
        this.frameSize = frameSize + 2;
        this.ACKTimeout = ACKTimeout;
        this.maxRetransmitTimes = maxRetransmitTimes;

        phy = new Physicis(4, this.frameSize + 2, "wire", true);

        macThread = new Thread(this);

        ACKData = new byte[frameSize - 2];

        for (int i = 0; i < frameSize - 2; i++) {
            if (i % 3 == 0) {
                ACKData[i] = 'A';
            } else if (i % 3 == 1) {
                ACKData[i] = 'C';
            } else {
                ACKData[i] = 'K';
            }
        }
    }

    public Mac(int addr) throws IOException {
        this(addr, 50, 500, 5);
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

    public MacFrame read() throws InterruptedException {
        return read(-1);
    }

    public MacFrame readNoWait() throws InterruptedException {
        return read(0);
    }

    public MacFrame read(long timeout) throws InterruptedException {
        if (timeout == -1) {
            return rxFrameBuffer.take();
        } else if (timeout == 0) {
            return rxFrameBuffer.poll();
        } else {
            return rxFrameBuffer.poll(timeout, TimeUnit.MILLISECONDS);
        }
    }

    public boolean write(MacFrame frame) throws InterruptedException {
        frame.src = addr;
        txFrameBuffer.put(frame);
        return txResultBuffer.take();
    }

    public boolean write(int dest, byte[] data) throws InterruptedException {
        MacFrame frame = new MacFrame(dest, addr, MacFrameType.DATA, MacFrame.FLAG_PLAIN, 0, data);
        return write(frame);
    }

    @Override
    public void run() {
        int rxFrameIdx = -1;

        int txFrameIdx = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {

                byte[] phyData = phy.read();
                if (phyData != null) {
                    if (phyData.length == frameSize) {
                        MacFrame frame = MacFrame.parseFrame(phyData);
                        if (frame.dest == addr) {
                            if (frame.idx == rxFrameIdx % 8) {
                                System.err.println("Recieve duplicated package: get " + frame.idx + "expect " + (rxFrameIdx + 1));
                            } else if (frame.idx == (rxFrameIdx + 1) % 8) {
                                rxFrameIdx++;
                                rxFrameBuffer.put(frame);
                                System.out.println("Receive" + rxFrameIdx + "from" + frame.src + "to" + frame.dest + ", Ack: " + frame.requireACK());
                            } else {
                                System.err.println("UnRecognized Package");
                            }
                            if (frame.requireACK()) {
                                sendACK(frame.src, frame.idx);
                            }
                        }
                    }
                } else {
                    MacFrame txFrame = txFrameBuffer.poll();
                    if (txFrame != null) {
                        System.out.println("transmitting" + txFrameIdx + "from" + txFrame.src + "to" + txFrame.dest);
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
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public int getFramePayloadSize() {
        return frameSize;
    }

    private void sendACK(int dest, int idx) throws InterruptedException {
        phy.write(new MacFrame(dest, addr, MacFrameType.ACK, MacFrame.FLAG_PLAIN, idx, ACKData).toBytes());
    }

}