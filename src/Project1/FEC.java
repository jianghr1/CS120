package Project1;

import physics.Physicis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.random.RandomGenerator;

public class FEC {

    public static void main(String[] args) throws IOException, InterruptedException {
        Physicis physics;
        String GT_input;
        int frameSize=55;

        physics = new Physicis(8, frameSize, "mic", true);

        Path path = Paths.get("INPUT.txt");
        char[] input_data = Files.readString(path).toCharArray();
        GT_input = String.valueOf(input_data);
        byte[] ori_data = new byte[frameSize * 23];
        Arrays.fill(ori_data, (byte) 0);
        for (int i = 0; i < input_data.length; i++) {
            ori_data[i/8] <<= 1;
            ori_data[i/8] |= (byte)((input_data[i] == '0') ? 0 : 1);
        }
        int numFrames = (int) Math.ceil((double) ori_data.length / frameSize);
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        physics.start();

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

        while (!physics.sendComplete()) {
            Thread.sleep(100);
        }
        Thread.sleep(400);

        System.out.println("read start");

        StringBuilder output = new StringBuilder();
        frame = physics.readNoWait();
        int cnt = 0;
        while (frame != null) {
            for (byte b : frame) {
                for (int i = 0; i < 8; i++) {
                    output.append((b & 0xff) > 127 ? "1" : "0");
                    b <<= 1;
                    cnt++;
                }
                if(cnt == GT_input.length()) break;
            }
            frame = physics.readNoWait();
        }
        path = Paths.get("OUTPUT.txt");
        Files.writeString(path, output.toString());
        physics.stop();
        cnt = 0;
        for (int i = 0; i < Math.min(GT_input.length(), output.length()); i++) {
            if (GT_input.charAt(i) == output.charAt(i)) {
                cnt++;
            }
        }
        path = Paths.get("GT.txt");
        Files.writeString(path, GT_input);
        System.out.println((double) cnt / GT_input.length());
    }
}
