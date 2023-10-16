package physics.base;

import java.util.ArrayList;

public class CarrierGenerator {
    private double sampleRate;
    private int bufferLength;

    public CarrierGenerator(double sampleRate, int bufferLength) {
        this.sampleRate = sampleRate;
        this.bufferLength = bufferLength;
    }

    public CarrierGenerator(double sampleRate) {
        this.sampleRate = sampleRate;
        this.bufferLength = 512;
    }

    public CarrierGenerator() {
        this.sampleRate = 48000;
        this.bufferLength = 512;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    private float carrierFunc(int baseFrequency, String carrierType, int step) {
        float x;
        if (carrierType.compareToIgnoreCase("sin") == 0) {
            x = (float) Math.sin(step / this.sampleRate * baseFrequency * 2 * Math.PI);
        } else if (carrierType.compareToIgnoreCase("pulse") == 0) {
            x = (Math.sin(step / this.sampleRate * baseFrequency * 2 * Math.PI) >= 0) ? 1.0f : -1.0f;
        } else if (carrierType.compareToIgnoreCase("Proj1-Task2") == 0) {
            x = (float) Math.sin(step / this.sampleRate * baseFrequency * 2 * Math.PI) + (float) Math.sin(step / this.sampleRate * 10 * baseFrequency * 2 * Math.PI);
        } else if (carrierType.compareToIgnoreCase("None") == 0) {
            x = .0f;
        } else if (carrierType.compareToIgnoreCase("Empty") == 0) {
            x = .0f;
        } else {
            throw new IllegalArgumentException("Unrecognized Carrier Type");
        }
        return x;
    }
    public ArrayList<float[]> genCarrier(int baseFrequency, String carrierType, int bufferLength, float carrierLength) {
        ArrayList<float[]> carriers = new ArrayList<> ();
        int numBuffers = Math.ceilDiv((int)(carrierLength * this.sampleRate), bufferLength);
        for (int i = 0; i < numBuffers; i++) {
            carriers.add(new float[bufferLength]);
        }
        for (int i = 0; i < numBuffers * bufferLength; i++){
            carriers.get(i / bufferLength)[i%bufferLength] = carrierFunc(baseFrequency, carrierType, i);
        }
        return carriers;
    }

    public ArrayList<float[]> genCarrier(int baseFrequency, String carrierType, float carrierLength) {
        return genCarrier(baseFrequency, carrierType, bufferLength, carrierLength);
    }

    public ArrayList<float[]> genCarrier(int baseFrequency, String carrierType, int bufferLength) {
        return genCarrier(baseFrequency, carrierType, bufferLength, -1.0f);
    }
    public ArrayList<float[]> genCarrier(int baseFrequency, String carrierType) {
        return genCarrier(baseFrequency, carrierType, this.bufferLength);
    }

    public ArrayList<float[]> genCarrier(int baseFrequency, float carrierLength) {
        return genCarrier(baseFrequency, "sin", carrierLength);
    }

    public ArrayList<float[]> genSilence(float carrierLength) {
        return genCarrier(0, "Empty", carrierLength);
    }
}
