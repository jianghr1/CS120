package mac;

import physics.Physicis;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MacReceiver implements Runnable {
    private final Mac mac;
    public long last_time = 10000;
    private final Physicis phy;
    private final LinkedBlockingQueue<MacFrame> rxFrameBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<MacFrame> txDispatchFrames;
    private final int addr;
    private final int frameSize;
    private final Thread macThread;
    private final byte[] ACKData;
    private boolean started = false;
    private int rxFrameIdx;
    public MacReceiver(Mac mac, Physicis phy, LinkedBlockingQueue<MacFrame> txDispatchFrames, int addr, int frameSize) {
        this.mac = mac;
        this.phy = phy;
        this.txDispatchFrames = txDispatchFrames;
        this.addr = addr;
        this.frameSize = frameSize;
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

    public void start() {
        if (started) {
            return;
        }
        macThread.start();
        phy.start();
        started = true;
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

    private void dataHandler(MacFrame frame) throws InterruptedException {
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

    private void ackHandler(MacFrame frame) throws InterruptedException {
        if (frame.dest == addr) {
            txDispatchFrames.put(frame);
        }
    }

    @Override
    public void run() {
        rxFrameIdx = -1;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] phyData = phy.read(10);
//                System.out.println("Pending Receive" + phyData.length);
                if (phyData != null && phyData.length == frameSize) {
                    MacFrame frame = MacFrame.parseFrame(phyData);
                    switch (frame.type) {
                        case DATA :
                            dataHandler(frame);
                            break;
                        case ACK :
                            ackHandler(frame);
                            break;
                    }
                    mac.setMacState(MACState.Tx_waiting);
                } else if (mac.isTxTimeOut()) {
                    mac.setMacState(MACState.Tx_waiting);
                }

            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void sendACK(int dest, int idx) throws InterruptedException {
        phy.write(new MacFrame(dest, addr, MacFrameType.ACK, MacFrame.FLAG_PLAIN, idx, ACKData).toBytes());
    }
    private void sendRTS(int dest, int idx) throws InterruptedException {
        phy.write(new MacFrame(dest, addr, MacFrameType.RTS, MacFrame.FLAG_PLAIN, idx, ACKData).toBytes());
    }

}
