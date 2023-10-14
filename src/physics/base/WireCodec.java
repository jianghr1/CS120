package physics.base;

import java.util.Arrays;
import java.util.Objects;

public class WireCodec {
    private float[] carrier0;
    private float[] carrier1;
    private Integer carrierLength;
    private String carrierType;
    WireCodec(Integer carrierLength, String carrierType) {
        this.carrierLength = carrierLength;
        this.carrierType = carrierType;
        genCarrier();
    }
    private void genCarrier() {
        this.carrier0 = new float[this.carrierLength];
        this.carrier1 = new float[this.carrierLength];
        if (this.carrierType.compareToIgnoreCase("halfSin") == 0) {
            for (int i = 0; i < this.carrierLength; i++) {
                this.carrier0[i] = + (float) Math.sin((double) i / this.carrierLength * Math.PI);
                this.carrier1[i] = - (float) Math.sin((double) i / this.carrierLength * Math.PI);
            }
        } else if (this.carrierType.compareToIgnoreCase("tiktok") == 0) {
            for (int i = 0; i < this.carrierLength; i++) {
                this.carrier0[i] = (float) Math.sin((double) i / this.carrierLength * Math.PI * 2);
            }
            Arrays.fill(this.carrier1, 0);
        } else {
            throw new IllegalArgumentException("Unrecognized Carrier Type");
        }
    }
//
//    public float[] encode(byte[] dataSeq) {
//        float[] output = new float[dataSeq.length * this.carrierLength];
//        for (byte dataBit : dataSeq) {
//
//        }
//    }
}
