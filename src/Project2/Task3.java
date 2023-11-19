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

    public void run(boolean transmitter) throws InterruptedException, IOException {
        int src_addr = (transmitter) ? 2 : 1;
        int dest_addr = (transmitter) ? 1 : 2;
        Mac mac = new Mac(src_addr);
        int frameSize = mac.getFramePayloadSize();
        int numFrames = (int) Math.ceil((double) ori_data.length / frameSize);

        mac.start();

        byte[] rec_data = new byte[ori_data.length];
        Thread.sleep(500);
        long start = System.currentTimeMillis();
        int frameIdx = 0;
        if (transmitter) {
            for (int i = 0; i < numFrames; i++) {
                MacFrame frame;
                byte[] data = Arrays.copyOfRange(ori_data, i * frameSize, (i + 1) * frameSize);

                frame = new MacFrame(dest_addr, src_addr, MacFrameType.DATA, i == numFrames - 1 ? MacFrame.FLAG_DATA_END : MacFrame.FLAG_PLAIN, 0, data);
                if (!mac.write(frame)) {
                    System.err.println("Send Package " + i + "Failed. Exiting");
                    break;
                }

                frame = mac.read(1000);
                if(frame == null)
                    break;
                System.arraycopy(frame.data, 0, rec_data, frameIdx * frameSize, frameSize);
                frameIdx++;
            }
        } else  {
            for (int i = 0; i < numFrames; i++) {
                MacFrame frame;

                frame = mac.read(1000);
                if(frame == null)
                    break;
                System.arraycopy(frame.data, 0, rec_data, frameIdx * frameSize, frameSize);
                frameIdx++;

                byte[] data = Arrays.copyOfRange(ori_data, i * frameSize, (i + 1) * frameSize);
                frame = new MacFrame(dest_addr, src_addr, MacFrameType.DATA, i == numFrames - 1 ? MacFrame.FLAG_DATA_END : MacFrame.FLAG_PLAIN, 0, data);
                if (!mac.write(frame)) {
                    System.err.println("Send Package " + i + "Failed. Exiting");
                    break;
                }
            }
        }
        System.out.println("Transmit complete in " + (System.currentTimeMillis() - start) + "ms");

        utils.writeBinaryFile("OUTPUT.bin", rec_data);
        mac.stop();
    }
}
