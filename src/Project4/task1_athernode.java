package Project4;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class task1_athernode {
    public static void main(String[] args) throws IOException {
        String query_site = "www.baidu.com";
        Mac mac = new Mac(1);

        try {
            Thread.sleep(1000);
            mac.start();
            Thread.sleep(1000);
            for (int i = 0; i < 10; i++) {
                byte[] data = new byte[20];
                byte[] query_site_binary = query_site.getBytes(StandardCharsets.US_ASCII);
                System.arraycopy(query_site_binary, 0, data, 0, query_site_binary.length);
                MacFrame frame = new MacFrame(0, 1, MacFrameType.DATA, MacFrame.FLAG_PLAIN, data);
                long time = System.nanoTime();
                mac.write(frame);
                MacFrame replyFrame = mac.read(1000);
                if (replyFrame == null)
                {
                    System.out.println("Lost");
                } else {
                    byte[] replyData = replyFrame.data;
                    String domain = new String(replyData, StandardCharsets.US_ASCII);
                    StringBuilder nonZeroChars = new StringBuilder();
                    for (int j = 0; j < domain.length(); j++) {
                        char c = domain.charAt(j);
                        if (c == 0X00) {
                            break;
                        }
                        else
                            nonZeroChars.append(c);
                    }
                    System.out.println("Destination ip " + nonZeroChars);
                    double ping = (System.nanoTime() - time) / 1000000.0;
                    System.out.println("DNS query finished " + ping + "ms");
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            mac.stop();
            throw new RuntimeException(e);
        }
        mac.stop();
    }
}
