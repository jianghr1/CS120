package Project0;

import physics.base.BaseAudioPhysics;
import physics.base.CarrierGenerator;

public class Task1 implements Runnable {
    private final BaseAudioPhysics baseAudioPhysics;
    private final CarrierGenerator carrierGenerator;
    private boolean stopReq = false;
    public Task1() {
        baseAudioPhysics = new BaseAudioPhysics();
        carrierGenerator = new CarrierGenerator(baseAudioPhysics.getSampleRate(), baseAudioPhysics.getBufferSize());
    }
    @Override
    public void run() {
        stopReq = false;
        float[] carrier = carrierGenerator.genCarrier(441);
        baseAudioPhysics.stop();
        while (!Thread.currentThread().isInterrupted() && !this.stopReq) {
            try {
                baseAudioPhysics.clearRxBuffer();
                /* play 10s tones and record */
                for (int i = 0; i < baseAudioPhysics.getSampleRate() * 10 / baseAudioPhysics.getBufferSize(); i++) {
                    baseAudioPhysics.put(carrier);
                }

                System.out.println("Start to play the tone and record");
                /* start to play & record */
                baseAudioPhysics.start();
                /* wait 10 seconds */
                Thread.sleep(10*1000);
                /* stop playing */
                baseAudioPhysics.stop();

                System.out.println("record ended");
                System.out.println("Transferring recorded waves to Tx buffer");
                float[] capturedWave = baseAudioPhysics.poll();
                while (capturedWave != null) {
                    baseAudioPhysics.put(capturedWave);
                    capturedWave = baseAudioPhysics.poll();
                }
                System.out.println("Recorded waves have been transferred to Tx buffer.");

                System.out.println("Start to play the recorded wave");
                baseAudioPhysics.start();
                Thread.sleep(10*1000);
                baseAudioPhysics.stop();
                break;

            } catch (InterruptedException e) {
                baseAudioPhysics.stop();
                return;
            }
        }
        baseAudioPhysics.stop();
    }
    public void reqStop() {
        System.out.println("Stop Request recieved. Thread should stop in 20 sec.");
        this.stopReq=true;
    }
}
