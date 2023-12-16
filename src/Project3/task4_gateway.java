package Project3;

import mac.Mac;
import mac.MacFrame;
import mac.MacFrameType;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class task4_gateway {
    private static boolean ather_ping(Inet4Address src, Inet4Address dst, Mac mac, byte[] requestPayload) throws InterruptedException {

        byte[] srcIP = src.getAddress();
        byte[] dstIP = dst.getAddress();
        byte[] requestData = new byte[mac.getFramePayloadSize()];
        boolean error = false;
        System.arraycopy(srcIP, 0, requestData, 0, 4);
        System.arraycopy(dstIP, 0, requestData, 4, 4);
        requestData[8] = 0;
        int numFragments = (int) Math.ceil((double) requestPayload.length / (mac.getFramePayloadSize()-10));
        System.out.println("num fragments = "+numFragments);
        for (int i = 0; i < numFragments; i++)
        {
            System.out.println("Handling "+i);
            int datalenght =  Math.min(requestPayload.length - i*numFragments, numFragments);
            requestData[9] = (byte) i;
            System.arraycopy(requestPayload, i*numFragments, requestData, 10, datalenght);
            MacFrame frame = new MacFrame(1, 0, MacFrameType.DATA, MacFrame.FLAG_PLAIN, requestData);
            mac.write(frame);
            frame = mac.read(1000);
            if (frame == null || frame.data[9] != (byte) i)
            {
                error=true;
                break;
            }
            System.arraycopy(frame.data, 10, requestPayload, i*numFragments, datalenght);
        }
        return error;
    }
    public static void main(String[] args) throws IOException, PcapNativeException, NotOpenException, InterruptedException {

        PcapNetworkInterface nif;
        try {
            nif = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nif == null) {
            return;
        }

        byte[] athernetIdentifying = {0x5a, 0x6b, 0x7c, (byte) 0x8d};

        System.out.println(nif.getName() + ":" + nif.getDescription());

        final Inet4Address address = (Inet4Address) nif.getAddresses().get(nif.getAddresses().size() - 1).getAddress();
        System.out.println("ipv4 address is " + address);
        final MacAddress MAC_ADDR = MacAddress.getByAddress(nif.getLinkLayerAddresses().get(0).getAddress());
        // WLAN 网关MAC
        final MacAddress GATEWAY_MAC_ADDR = MacAddress.getByName("00-00-5e-00-01-01", "-");
        System.out.println("MAC address is " + MAC_ADDR);

        final PcapHandle captureHandle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        final PcapHandle sendHandle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        captureHandle.setFilter("icmp or arp", BpfProgram.BpfCompileMode.OPTIMIZE);

        UnknownPacket.Builder payloadBuilder = new UnknownPacket.Builder();
        IcmpV4EchoReplyPacket.Builder icmpV4EchoReplyBuiler = new IcmpV4EchoReplyPacket.Builder();
        icmpV4EchoReplyBuiler.payloadBuilder(payloadBuilder);
        IcmpV4CommonPacket.Builder icmpV4CommonBuilder = new IcmpV4CommonPacket.Builder();
        icmpV4CommonBuilder
                .type(IcmpV4Type.ECHO_REPLY)
                .code(IcmpV4Code.NO_CODE)
                .payloadBuilder(icmpV4EchoReplyBuiler)
                .correctChecksumAtBuild(true);
        IpV4Packet.Builder ipv4Builder = new IpV4Packet.Builder();
        ipv4Builder
                .srcAddr(address)
                .version(IpVersion.IPV4)
                .tos(IpV4Rfc791Tos.newInstance((byte) 0))
                .ttl((byte) 100)
                .protocol(IpNumber.ICMPV4)
                .identification((short) 100)
                .payloadBuilder(icmpV4CommonBuilder)
                .correctChecksumAtBuild(true)
                .correctLengthAtBuild(true);
        EthernetPacket.Builder ethernetBuilder = new EthernetPacket.Builder();
        ethernetBuilder
                .srcAddr(MAC_ADDR)
                .dstAddr(GATEWAY_MAC_ADDR)
                .type(EtherType.IPV4)
                .payloadBuilder(ipv4Builder)
                .paddingAtBuild(true);

        final LinkedBlockingQueue<IpV4Packet> pingPackages = new LinkedBlockingQueue<>();

        final PacketListener listener = packet -> {
            if (packet.contains(IcmpV4EchoPacket.class)) {
                try {
                    System.out.println("got a input");
                    pingPackages.put(packet.get(IpV4Packet.class));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (true) {
                try {
                    captureHandle.loop(-1, listener);
                } catch (PcapNativeException e) {
                    e.printStackTrace();
                } catch (InterruptedException | NotOpenException e) {
                    break;
                }
            }
        });

        Thread.sleep(2000);

//        Mac mac = new Mac(0, 50, 250, 1);
        try {
//            mac.start();
            while(true) {
                IpV4Packet requestPacket = pingPackages.take();
                IcmpV4EchoPacket icmpV4Echopacket = requestPacket.get(IcmpV4EchoPacket.class);
                Inet4Address dst = requestPacket.getHeader().getDstAddr();
                Inet4Address src = requestPacket.getHeader().getSrcAddr();
                byte[] requestPayload = icmpV4Echopacket.getPayload().getRawData();
                System.out.println("Receiving a package from "+src.toString() + "to"+dst.toString());
                System.out.println("15:"+requestPayload[15]+"16:"+requestPayload[16]+"17:"+requestPayload[17]);
                if (dst.equals(address)) {
                    int i = 0;
                    for (;i < athernetIdentifying.length && i < requestPayload.length; i++)
                    {
                        if (requestPayload[i+16] != athernetIdentifying[i])
                            break;
                    }
                    if (i == athernetIdentifying.length)
                    {
                        System.out.println("package to athernet");
//                        if (!ather_ping(src, dst, mac, requestPayload)) {
//                            System.err.println("A Package to ather net drop due to time out");
//                            continue;
//                        }
                    }
                } else {
                    if (dst.getAddress()[0] == (byte)172 && dst.getAddress()[1]==(byte)18)
                    {
//                        if (!ather_ping(src, dst, mac, requestPayload)) {
//                            System.err.println("A Package to ather net drop due to time out");
//                            continue;
//                        }
                    }
                }
                ipv4Builder.dstAddr(src);
                ipv4Builder.srcAddr(dst);
                icmpV4EchoReplyBuiler.identifier(icmpV4Echopacket.getHeader().getIdentifier());
                icmpV4EchoReplyBuiler.sequenceNumber(icmpV4Echopacket.getHeader().getSequenceNumber());
                payloadBuilder.rawData(requestPayload);
                EthernetPacket packet = ethernetBuilder.build();
                sendHandle.sendPacket(packet);
            }

        } catch (InterruptedException e) {
//            mac.stop();
            throw new RuntimeException(e);
        }
    }
}
