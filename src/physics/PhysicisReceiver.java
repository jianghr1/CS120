package physics;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;

enum PhysicalReceiverState {
    DETECT,
    DECODE,
    COOLDOWN
}
public class PhysicisReceiver implements Runnable{
    private final LinkedBlockingQueue<float []> rxAudioBuffer;
    private final LinkedBlockingQueue<byte []> rxDataBuffer;
    private final int bitWidth;
    private final Codec codec;
    private final int frameSize;
    private final boolean isHamming;
    public PhysicisReceiver(LinkedBlockingQueue<float[]> rxAudioBuffer,
                               LinkedBlockingQueue<byte[]> rxDataBuffer,
                               int bitWidth,
                               Codec codec,
                               int frameSize,
                               boolean isHamming) {
        this.rxAudioBuffer = rxAudioBuffer;
        this.rxDataBuffer = rxDataBuffer;
        this.bitWidth = bitWidth;
        this.codec = codec;
        this.frameSize = frameSize;
        this.isHamming = isHamming;
    }
    @Override
    public void run() {
        float[] preamble = codec.getPreamble();
        PhysicalReceiverState state = PhysicalReceiverState.DETECT;
        float power = 0f;
        int startIndex = 0;
        Queue<Float> syncQueue = new LinkedList<>();
        for (int i = 0; i < preamble.length; i++) {
            syncQueue.add(0f);
        }
        float syncPowerLocalMax = 0f;

        int numSamples = bitWidth * frameSize * 8;

        if (isHamming)
        {
            numSamples = bitWidth * (int) (Math.ceil(frameSize * 8 / 120f) * 127);
        }
        System.out.println(numSamples);
        float[] decode = new float[numSamples];
        int currentDecodeIndex = 0;
        int totalCount = 0;
        int dataIndex = 0;
        int coolDown = 0;

        float[] previousData = null;

        float syncPowerThreshold = 0.08f;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                float[] data = rxAudioBuffer.take();
                if (previousData == null) {
                    previousData = new float[data.length];
                }
                int lastCount = totalCount;
                totalCount += data.length;

                while (dataIndex < totalCount) {
                    float currentSample = data[dataIndex - lastCount];
                    power = power * 63 / 64 + currentSample * currentSample / 64;

                    if (state == PhysicalReceiverState.DETECT) {
                        syncQueue.poll();
                        syncQueue.add(currentSample);
                        int i = 0;
                        float syncPower = 0;
                        for (float sample : syncQueue) {
                            syncPower += sample * preamble[i++];
                        }
                        if (syncPower > power && syncPower > syncPowerLocalMax && syncPower > syncPowerThreshold) {
                            syncPowerLocalMax = syncPower;
                            startIndex = dataIndex + 1;
                        } else if (dataIndex - startIndex + 1 > preamble.length / 2 && startIndex != 0) {
                            syncPowerLocalMax = 0;
                            syncQueue.clear();
                            for (int j = 0; j < preamble.length; j++) {
                                syncQueue.add(0f);
                            }
                            state = PhysicalReceiverState.DECODE;
//                            System.out.println("go to Decode");
                            if (startIndex < lastCount) {
                                int j = 0;
                                for (; j < lastCount - startIndex; j++) {
                                    decode[j] = previousData[previousData.length - lastCount + startIndex - j];
                                }
                                for (; j <= dataIndex - startIndex; j++) {
                                    decode[j] = data[j - lastCount + startIndex];
                                }
                            } else {
                                if (dataIndex + 1 - startIndex >= 0) {
                                    System.arraycopy(data, startIndex - lastCount, decode, 0, dataIndex + 1 - startIndex);
                                }
                            }
                            currentDecodeIndex = dataIndex - startIndex + 1;
                        }
                    } else if (state == PhysicalReceiverState.DECODE) {
                        decode[currentDecodeIndex++] = currentSample;
                        if (currentDecodeIndex == decode.length) {
                            byte[] decoded = codec.demodulate(decode, bitWidth);
                            validateAndPutData(decoded);
                            startIndex = 0;
                            currentDecodeIndex = 0;
                            state = PhysicalReceiverState.COOLDOWN;
//                            System.out.println("go to cool");
                            coolDown = 0;
                        }
                    } else {
                        coolDown++;
                        if (coolDown >= preamble.length) {
                            state = PhysicalReceiverState.DETECT;
//                            System.out.println("go to detect");
                        }
                    }
                    dataIndex += 1;
                }
                previousData = data.clone();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void validateAndPutData(byte[] decoded) throws InterruptedException {
        byte[] data = Arrays.copyOf(decoded, frameSize - 2);
        int crc = utils.getCRC(data);
        if (((crc >> 8) & 0xFF) == (decoded[frameSize - 2] & 0xFF) && (crc & 0xFF) == (decoded[frameSize - 1] & 0xFF)) {
            rxDataBuffer.put(data);
        }
        else {
            System.err.println("CRC error. Dropped");
//            rxDataBuffer.put(data);
        }
    }
}
