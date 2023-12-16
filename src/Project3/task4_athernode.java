package Project3;

import com.sun.net.httpserver.Request;
import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class task4_athernode {
    public static void main(String[] args) throws IOException {
        byte[] address = {(byte) 172, (byte) 18, 1, 2};

        Mac mac = new Mac(1, 50, 250, 1);

        try {
            Thread.sleep(1000);
            mac.start();
            Thread.sleep(1000);
            MacFrame frame = mac.read(10000);
            while(frame != null) {
                byte[] data = frame.data;
                byte[] srcIP = Arrays.copyOfRange(data, 0, 4);
                byte[] dstIP = Arrays.copyOfRange(data, 4, 8);
                Inet4Address src = (Inet4Address) Inet4Address.getByAddress(srcIP);
                Inet4Address dst = (Inet4Address) Inet4Address.getByAddress(dstIP);
                System.out.println("Receive request from "+ src + "to"+dst);
                System.arraycopy(srcIP, 0, data, 4, 4);
                System.arraycopy(dstIP, 0, data, 0, 4);
                mac.write(new MacFrame(0, 1, MacFrameType.DATA, MacFrame.FLAG_PLAIN, data));
                frame = mac.read(10000);
            }
        } catch (InterruptedException e) {
            mac.stop();
            throw new RuntimeException(e);
        }
        mac.stop();
    }
}
