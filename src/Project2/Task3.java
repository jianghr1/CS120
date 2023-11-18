package Project2;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;
import physics.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.random.RandomGenerator;

public class Task3 {
    String GT_input;
    byte[] ori_data;

    public Task3() throws IOException, InterruptedException {
        ori_data = new byte[6250];
        RandomGenerator.getDefault().nextBytes(ori_data);
//        ori_data = utils.readBinaryFile("INPUT.bin");
    }

    public void run(boolean transmitter, boolean receiver) throws InterruptedException, IOException {
//        int src_addr = (transmitter && receiver) ? 0 : 1;
//        int dest_addr = (transmitter && receiver) ? 0 : 2;
        int src_addr = 1;
        int dest_addr = 2;
        Mac mac = new Mac(transmitter ? src_addr : dest_addr);
        int frameSize = mac.getFramePayloadSize();
        int numFrames = (int) Math.ceil((double) ori_data.length / frameSize);

        mac.start();

        Thread.sleep(500);
        if (transmitter) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < numFrames; i++) {
                byte[] data = Arrays.copyOfRange(ori_data, i * frameSize, (i + 1) * frameSize);
                MacFrame frame = new MacFrame(dest_addr, src_addr, MacFrameType.DATA, i == numFrames - 1 ? MacFrame.FLAG_DATA_END : MacFrame.FLAG_PLAIN, 0, data);
                if (!mac.write(frame)) {
                    System.err.println("Send Package " + i + "Failed. Exiting");
                    //break;
                }
            }
            System.out.println("Transmit complete in " + (System.currentTimeMillis() - start) + "ms");
        }

        if (receiver) {
            byte[] rec_data = new byte[ori_data.length];
            int frameIdx = 0;
            MacFrame frame = mac.readNoWait();
            while (frame != null && !frame.dataEnd()) {
                System.arraycopy(frame.data, 0, rec_data, frameIdx * frameSize, frameSize);
                frameIdx++;
                frame = mac.read(1000);
            }
            utils.writeBinaryFile("OUTPUT.bin", rec_data);
        }
        mac.stop();
    }
}
