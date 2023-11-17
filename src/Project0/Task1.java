package Project0;

import physics.base.BaseAudioPhysics;
import physics.base.CarrierGenerator;

import java.util.ArrayList;

public class Task1 implements Runnable {
    private final BaseAudioPhysics baseAudioPhysics;
    private final CarrierGenerator carrierGenerator;

    public Task1() {
        baseAudioPhysics = new BaseAudioPhysics();
        carrierGenerator = new CarrierGenerator(baseAudioPhysics.getSampleRate(), baseAudioPhysics.getBufferSize());
    }

    @Override
    public void run() {
        ArrayList<float[]> waves = carrierGenerator.genCarrier(441, 10.0f);
        baseAudioPhysics.stop();
        try {
            System.out.println("Start to play the waves");
            baseAudioPhysics.playAndRecord(waves);
            System.out.println("play ended");

            waves = carrierGenerator.genSilence(10.0f);
            System.out.println("Start to record");
            baseAudioPhysics.playAndRecord(waves);
            System.out.println("record ended");

            waves = baseAudioPhysics.tranferRecordToPlay();

            System.out.println("Start to play the waves");
            baseAudioPhysics.playAndRecord(waves);
            System.out.println("play ended");

        } catch (InterruptedException e) {
            baseAudioPhysics.stop();
            return;
        }
        baseAudioPhysics.stop();
        baseAudioPhysics.release();
    }
}
