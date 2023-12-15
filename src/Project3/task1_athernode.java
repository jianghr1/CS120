package Project3;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public class task1_athernode {
    public static void main(String[] args) throws IOException {
        final byte[] srcIP = {(byte) 192, (byte) 168, 1, 2};
        // final byte[] dstIP = {119, 75, (byte) 217, 26};
        final byte[] dstIP = {(byte) 10, (byte) 20, (byte) 93, (byte) 61};
        Mac mac = new Mac(1);

        try {
            Thread.sleep(1000);
            mac.start();
            for (int i = 0; i < 10; i++) {
                byte[] data = new byte[20];
                System.arraycopy(srcIP, 0, data, 0, 4);
                System.arraycopy(dstIP, 0, data, 4, 4);
                data[8] = (byte) ((i >> 8) & 0xFF);
                data[9] = (byte) (i & 0xFF);
                byte[] payload = "Athernet!!".getBytes(StandardCharsets.UTF_8);
                System.arraycopy(payload, 0, data, 10, 10);
                MacFrame frame = new MacFrame(0, 1, MacFrameType.DATA, MacFrame.FLAG_PLAIN, data);
                long time = System.nanoTime();
                mac.write(frame);
                MacFrame replyFrame = mac.read(5000);
                if (replyFrame == null)
                {
                    mac.stop();
                    break;
                }
                byte[] replyData = replyFrame.data;
                byte[] replySrcIP = Arrays.copyOf(replyData, 4);
                byte[] replyDstIP = Arrays.copyOfRange(replyData, 4, 8);
                byte[] replyPayload = Arrays.copyOfRange(replyData, 10, 10);
                System.out.println("Receive ICMP Echo Reply from ip " + InetAddress.getByAddress(replySrcIP));
                System.out.println("Destination ip " + InetAddress.getByAddress(replyDstIP));
                System.out.println("Raw payload " + Arrays.toString(replyPayload));
                double ping = (System.nanoTime() - time) / 1000000.0;
                System.out.println("Ping " + ping + "ms");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            mac.stop();
            throw new RuntimeException(e);
        }
        mac.stop();
    }
}
