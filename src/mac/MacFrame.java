package mac;

import java.util.Arrays;

public class MacFrame {
    public static final int FLAG_PLAIN = 0;
    public static final int FLAG_DATA_END = 1;

    public final int dest;
    public final MacFrameType type;
    public final int flags;
    public final byte[] data;
    public int src;
    public int idx;

    public MacFrame(int dest, int src, MacFrameType type, int flags, int idx, byte[] data) {
        assert dest < 0xF;
        assert src < 0xF;
        assert flags < 0x3;
        assert idx < 0x7;
        this.dest = dest;
        this.src = src;
        this.type = type;
        this.flags = flags;
        this.data = data;
        this.idx = idx;
    }

    public static MacFrame parseFrame(byte[] frame) {
        int dest = frame[0] >> 4;
        int src = frame[0] & 0xF;
        MacFrameType type = MacFrameType.valueOf(frame[1] >> 5);
        int flags = frame[1] >> 3 & 0x3;
        int idx = frame[1] & 0x7;
        byte[] data = Arrays.copyOfRange(frame, 2, frame.length);
        return new MacFrame(dest, src, type, flags, idx, data);
    }

    public boolean requireACK() {
        return type == MacFrameType.DATA;
    }

    public boolean dataEnd() {
        return (flags & FLAG_DATA_END) != 0;
    }

    public byte[] toBytes() {
        byte[] result = new byte[data.length + 2];
        result[0] = (byte) (dest << 4 | src);
        result[1] = (byte) ((type.getValue() << 5) | (flags << 3) | (idx));
        System.arraycopy(data, 0, result, 2, data.length);
        return result;
    }
}