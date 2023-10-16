package Project1;

import physics.base.BaseAudioPhysics;
import physics.base.CarrierGenerator;

import java.util.ArrayList;

public class Task2  implements Runnable {
    private final BaseAudioPhysics baseAudioPhysics;
    private final CarrierGenerator carrierGenerator;
    public Task2() {
        baseAudioPhysics = new BaseAudioPhysics();
        carrierGenerator = new CarrierGenerator(baseAudioPhysics.getSampleRate(), baseAudioPhysics.getBufferSize());
    }

    @Override
    public void run() {
        ArrayList<float[]> waves = carrierGenerator.genCarrier(1000, "Proj1-Task2", 10.0f);
        baseAudioPhysics.stop();
        try {
            System.out.println("Start to play the waves");
            baseAudioPhysics.playAndRecord(waves);
            System.out.println("play ended");
        } catch (InterruptedException e) {
            baseAudioPhysics.stop();
            return;
        }
        baseAudioPhysics.stop();
    }
}
