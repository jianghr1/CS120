package Project3;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;

import java.net.*;
import java.util.Arrays;
import java.io.IOException;

public class task1_gateway {
    public static void main(String[] args) throws IOException {
        Mac mac = new Mac(0);
        MacFrame frame;
        try {
            mac.start();
            frame = mac.read(5000);
            while (frame != null) {
                byte[] data = frame.data;
                System.out.println(Arrays.toString(data));
                byte[] srcIP = Arrays.copyOf(data, 4);
                byte[] dstIP = Arrays.copyOfRange(data, 4, 8);
                int id = ((data[8] & 0xFF) << 8) | (data[9] & 0xFF);
                byte[] payload = Arrays.copyOfRange(data, 10, 20);
                System.out.println("payload " + Arrays.toString(payload));
                System.out.println((Inet4Address) InetAddress.getByAddress(dstIP));
                MacFrame frame_back = new MacFrame(frame.src, frame.dest, MacFrameType.DATA, MacFrame.FLAG_PLAIN, data);
                System.out.println(mac.write(frame_back));
                frame = mac.read(5000);
            }
        } catch (InterruptedException e) {
            mac.stop();
            throw new RuntimeException(e);
        }
    }
}
