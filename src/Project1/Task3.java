package Project1;

import physics.Physicis;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringJoiner;

public class Task3{
    private final Physicis physics;
    public Task3(int frameSize) throws IOException, InterruptedException {
        physics = new Physicis(8, frameSize);

        Path path = Paths.get("INPUT.txt");
        char[] input_data = Files.readString(path).toCharArray();
        byte[] ori_data = new byte[(input_data.length+7)/8];
        Arrays.fill(ori_data, (byte) 0);
        for (int i = 0; i < input_data.length; i++) {
            ori_data[i/8] <<= 1;
            ori_data[i/8] |= (byte)((input_data[i] == '0') ? 0 : 1);
        }
        int numFrames = (int) Math.ceil((double) ori_data.length / frameSize);
        for (int i = 0; i < numFrames - 1; i++) {
            byte[] frame = Arrays.copyOfRange(ori_data, i * frameSize, (i+1) * frameSize);
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
        Thread.sleep(100);
        StringBuilder output = new StringBuilder();
        byte[] frame = physics.readNoWait();
        while (frame != null) {
            for (byte b : frame){
                for (int i = 0; i < 8; i++)
                {
                    output.append((b & 0xff) > 127 ? "1" : "0");
                    b <<= 1;
                }
            }
            frame= physics.readNoWait();
        }
        Path path = Paths.get("OUTPUT.txt");
        Files.writeString(path, output.toString());
        physics.stop();
    }
}
