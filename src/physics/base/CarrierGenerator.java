package physics.base;

public class CarrierGenerator {
    private double sampleRate;
    private int carrierLength;

    public CarrierGenerator(double sampleRate, int carrierLength) {
        this.sampleRate = sampleRate;
        this.carrierLength = carrierLength;
    }

    public CarrierGenerator(double sampleRate) {
        this.sampleRate = sampleRate;
        this.carrierLength = 512;
    }

    public CarrierGenerator() {
        this.sampleRate = 44100;
        this.carrierLength = 512;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setCarrierLength(int carrierLength) {
        this.carrierLength = carrierLength;
    }

    public float[] genCarrier(int frequency, String carrierType, int carrierLength) {
        float[] carrier = new float[carrierLength];
        if (carrierType.compareToIgnoreCase("sin") == 0) {
            for (int i = 0; i < carrierLength; i++) {
                carrier[i] = (float) Math.sin(i / this.sampleRate * frequency * 2 * Math.PI);
            }
        } else if (carrierType.compareToIgnoreCase("pulse") == 0) {
            for (int i = 0; i < carrierLength; i++) {
                carrier[i] = (Math.sin(i / this.sampleRate * frequency * 2 * Math.PI) >= 0) ? 1.0f : -1.0f;
            }
        } else {
            throw new IllegalArgumentException("Unrecognized Carrier Type");
        }
        return carrier;
    }

    public float[] genCarrier(int frequency, String carrierType) {
        return genCarrier(frequency, carrierType, this.carrierLength);
    }

    public float[] genCarrier(int frequency) {
        return genCarrier(frequency, "sin", this.carrierLength);
    }
}
