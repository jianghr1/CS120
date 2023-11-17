package Project2;

import physics.Physicis;

import java.io.IOException;
import java.util.Arrays;
import java.util.random.RandomGenerator;

public class Task1 {
    private final Physicis physics;
    String GT_input;

    public Task1(int frameSize) throws IOException, InterruptedException {
        physics = new Physicis(4, frameSize, "wire", true);

        byte[] ori_data = new byte[1 << 14];
        Arrays.fill(ori_data, (byte) 0);

        RandomGenerator.getDefault().nextBytes(ori_data);
        StringBuilder temp = new StringBuilder();
        for (byte b : ori_data) {
            for (int i = 0; i < 8; i++) {
                temp.append((b & 0xff) > 127 ? "1" : "0");
                b <<= 1;
            }
        }
        GT_input = temp.toString();
        int numFrames = (int) Math.ceil((double) ori_data.length / frameSize);
        for (int i = 0; i < numFrames - 1; i++) {
            byte[] frame = Arrays.copyOfRange(ori_data, i * frameSize, (i + 1) * frameSize);
            physics.write(frame);
        }
        byte[] frame = new byte[frameSize];
        for (int i = (numFrames - 1) * frameSize; i < numFrames * frameSize; i++) {
            int frameIndex = i - (numFrames - 1) * frameSize;
            if (i < ori_data.length) {
                frame[frameIndex] = ori_data[i];
            } else {
                frame[frameIndex] = 0;
            }
        }
        physics.write(frame);
    }

    public void run() throws InterruptedException, IOException {
        physics.start();
        while (!physics.sendComplete()) {
            Thread.sleep(100);
        }
        Thread.sleep(400);
        StringBuilder output = new StringBuilder();
        byte[] frame = physics.readNoWait();
        int framesize = 0;
        while (frame != null) {
            framesize = frame.length * 8;
            for (byte b : frame) {
                for (int i = 0; i < 8; i++) {
                    output.append((b & 0xff) > 127 ? "1" : "0");
                    b <<= 1;
                }
            }
            frame = physics.readNoWait();
        }
//        Path path = Paths.get("OUTPUT.txt");
//        Files.writeString(path, output.toString());
        physics.stop();
        System.out.println("frame size=" + framesize + "bits");
        System.out.println("num PKGS=" + GT_input.length() / framesize);
        System.out.println("GT Bits=" + GT_input.length());
        System.out.println("REC Bits=" + output.length());
        boolean[] AC = new boolean[GT_input.length() / framesize];
        Arrays.fill(AC, true);
        int cnt = 0;
        for (int i = 0; i < Math.min(GT_input.length(), output.length()); i++) {
            if (GT_input.charAt(i) == output.charAt(i)) {
                cnt++;
            } else {
                System.out.println("error at package" + (i / framesize) + "@" + i % framesize);
                AC[i / framesize] = false;
            }
        }
        System.out.println(Arrays.toString(AC));
        int cnt2 = 0;
        for (int i = 0; i < AC.length; i++) {
            if (AC[i]) cnt2++;
            else System.err.println("Fail at" + i);
        }

//        path = Paths.get("GT.txt");
//        Files.writeString(path, GT_input);
        System.out.println((double) cnt / GT_input.length());
        System.out.println((double) cnt2 / AC.length);
    }
}
