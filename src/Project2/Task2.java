package Project2;

import physics.utils;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;

import java.io.IOException;
import java.util.Arrays;
import java.util.random.RandomGenerator;

public class Task2 {
    String GT_input;
    byte[] ori_data;
    Mac mac;
    public Task2() throws IOException, InterruptedException {
        mac = new Mac(1);
        ori_data = new byte[6250];
        RandomGenerator.getDefault().nextBytes(ori_data);
        ori_data = utils.readBinaryFile("INPUT.bin");
    }

    public void run(boolean transmitter, boolean receiver) throws InterruptedException, IOException {
        int frameSize = mac.getFramePayloadSize();
        int numFrames = (int) Math.ceil((double) ori_data.length / frameSize);

        mac.start();

        Thread.sleep(500);
        if (transmitter) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < numFrames; i++) {
                byte[] data = Arrays.copyOfRange(ori_data, i * frameSize, (i+1) * frameSize);
                MacFrame frame = new MacFrame(1, 0, MacFrameType.DATA, i == numFrames-1?MacFrame.FLAG_DATA_END:MacFrame.FLAG_PLAIN, 0, data);
                if (!mac.write(frame)) {
                    System.err.println("Send Package "+ i + "Failed. Exiting");
                    break;
                }
            }
            System.out.println("Transmit complete in " + (System.currentTimeMillis() - start) + "ms");
        }

        if (receiver) {
            byte[] rec_data = new byte[ori_data.length];
            int frameIdx = 0;
            MacFrame frame = mac.read();
            while (frame != null && !frame.dataEnd()) {
                System.arraycopy(frame.data, 0, rec_data, frameIdx * frameSize, frameSize);
                frameIdx++;
                frame = mac.read();
            }
            utils.writeBinaryFile("OUTPUT.bin", rec_data);
        }
        mac.stop();
    }
}
