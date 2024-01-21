package Project4;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class task1_gateway {
    public static void main(String[] args) throws IOException, PcapNativeException, NotOpenException, InterruptedException {


        Mac mac = new Mac(0);
        try {
            mac.start();
            while(true) {
                byte[] data = mac.read().data;
                System.out.println(Arrays.toString(data));
                String domain = new String(data, StandardCharsets.US_ASCII);

                StringBuilder nonZeroChars = new StringBuilder();
                for (int i = 0; i < domain_.length(); i++) {
                    char c = domain_.charAt(i);
                    if (c == 0X00) {
                        break;
                    }
                    else
                        nonZeroChars.append(c);
                }
                Process process = Runtime.getRuntime().exec("ping " + nonZeroChars);

                BufferedReader inputStream = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String s = "";

                byte[] replyData = new byte[mac.getFramePayloadSize()];

                while ((s = inputStream.readLine()) != null) {
                    System.out.println(s);
                    Pattern pattern = Pattern.compile("\\[([0-9\\.]+)\\]");
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find()) {
                        replyData = matcher.group(1).getBytes(StandardCharsets.US_ASCII);
                        break;
                    }
                }

                System.out.println("reply " + Arrays.toString(replyData));
                MacFrame frame = new MacFrame(1, 0, MacFrameType.DATA, MacFrame.FLAG_PLAIN, replyData);
                System.out.println(mac.write(frame));
            }

        } catch (InterruptedException e) {
            mac.stop();
            throw new RuntimeException(e);
        }
    }
}
