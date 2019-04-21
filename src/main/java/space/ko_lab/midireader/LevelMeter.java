/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader;

/**
 *
 * @author Laurens
 */


import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import space.ko_lab.midireader.core.Note;
import space.ko_lab.midireader.manipulation.Mapping;
import space.ko_lab.midireader.player.Instrument;
import space.ko_lab.midireader.player.LocalPlayer;
import space.ko_lab.myutils.MyTimer;

public class LevelMeter{

    static LocalPlayer player = new LocalPlayer();
    static String toTest[] = "F1,F2,F3,F4,F5,F6,F7,F8,P1,P2,P3".split(",");
    //static Instrument floppy;
    static int delay = 100;
    static int initDelay = 200;
    static int samples = 15;//norm 15
    static int startNote = 30;
    static int endNote = 90;
    static boolean micTest = false;
    
    static MyTimer startTime;
    static long delta = 0;
    public static void main(String[] args)throws Exception
    {
        //TODO adapt to new drive system!
        Thread t = new Thread(new Recorder(new LevelMeter()));
        t.start();
        System.out.println("mic started");
        while(micTest)
        {
            System.out.println(takeSample(true));
        }
        System.out.println("starting instruments...");
        Instrument.loadInstruments(new File("instruments.csv"));
        Mapping.loadMapList(new File("mappings.csv"));
        //floppy = Instrument.findInstrument("F8");
        player.init();
        Thread.sleep(1000);
        System.out.println("instruments started");
        double playLength = (initDelay + delay * (samples + 1)) /1000.0;
        System.out.println("playLength: " + playLength);
        System.out.println("generating test sequence");
        delta = 0;
        runThrough(false, playLength, new Sample(0, 0));
        double ETA = (double)delta / 1000000000.0;
        System.out.println("ETA: " + ETA + " seconds, or " + (ETA / 60.0) + " minutes, or " + (ETA / 60.0 / 60.0) + " hours");
        System.out.println("starting test in 10 seconds...");
        Thread.sleep(10000);
        //perform calibration
        System.out.println("calibrating...");
        delta = 0;
        startTime = new MyTimer();
        Sample calibration = takeSample(true);
        System.out.println("calibration: " + calibration);
        System.out.println("starting test sequence");
        //print header:
        System.out.println();
        print("note", true);
        for(String test:toTest)
        {
            print("\tp" + test, true);
            print("\tr" + test, true);
        }
        print("\n", true);
        //start test sequence:
        delta = 0;
        startTime = new MyTimer();
        
        player.play(startTime.getTimestamp());
        runThrough(true, playLength, calibration);
        
        //fc.noNote(testDrive);
        System.out.println();
        System.out.println("done!");
        t.stop();
    }
    public static void runThrough(boolean mode, double playLength, Sample calibration)throws Exception
    {
        int curNote = startNote;
        while(curNote != endNote)
        {
            print(curNote + "", mode);
            for(String test:toTest)
            {
                playNow(new Note(curNote), test, playLength, mode);
                delay(initDelay, mode);
                Sample sample = takeSample(mode);
                sample.offset(calibration);
                print("\t" + sample, mode);
            }
            print("\n", mode);
            
            
            curNote++;
        }
    }
    public static void print(String text, boolean mode)
    {
        if(mode == true)System.out.print(text);
    }
    public static void delay(int delay, boolean mode)throws InterruptedException
    {
        delta += delay * 1000000;//convert to nanoseconds
        if(mode == true)
        {
            while(startTime.hasPassedNan(delta) == false)
            {
                Thread.sleep(1);
            }
        }
    }
    public static Sample takeSample(boolean mode)throws InterruptedException
    {
        
        double rms = 0.0, peak = 0.0;
        int i = 0;
        while(i != samples)
        {
            delay(delay, mode);
            rms += Recorder.prms;
            peak += Recorder.ppeak;
            i++;
        }
        rms /= (double)samples;
        peak /= (double)samples;
        //Thread.sleep(delay);
        return new Sample(rms, peak);
    }
    public static class Sample
    {
        double rms, peak;

        public Sample(double rms, double peak)
        {
            this.rms = rms;
            this.peak = peak;
        }
        public void offset(Sample cal)
        {
            rms -= cal.rms;
            peak -= cal.peak;
        }

        @Override
        public String toString()
        {
            return peak + "\t" + rms;
        }
        
    }
    public static void playNow(Note note, String channel, double time, boolean mode)throws Exception
    {
        if(mode == true)return;//don't generate if it's live, it's already done then
        Instrument.findNote(channel, note);
        for(Instrument i:Instrument.list)i.commit(delta, time);
        //floppy.setNote(channel, note);
        //floppy.commit(delta, time);
    }
    static class Recorder implements Runnable
    {
        final LevelMeter meter;
        public static double prms,ppeak;
        Recorder(final LevelMeter meter) {
            this.meter = meter;
        }

        @Override
        public void run()
        {
            AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
            final int bufferByteSize = 2048;

            TargetDataLine line;
            try {
                line = AudioSystem.getTargetDataLine(fmt);
                line.open(fmt, bufferByteSize);
            } catch(LineUnavailableException e) {
                System.err.println(e);
                return;
            }

            byte[] buf = new byte[bufferByteSize];
            float[] samples = new float[bufferByteSize / 2];

            float lastPeak = 0f;

            line.start();
            for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

                // convert bytes to samples here
                for(int i = 0, s = 0; i < b;) {
                    int sample = 0;

                    sample |= buf[i++] & 0xFF; // (reverse these two lines
                    sample |= buf[i++] << 8;   //  if the format is big endian)

                    // normalize to range of +/-1.0f
                    samples[s++] = sample / 32768f;
                }

                float rms = 0f;
                float peak = 0f;
                for(float sample : samples) {

                    float abs = Math.abs(sample);
                    if(abs > peak) {
                        peak = abs;
                    }

                    rms += sample * sample;
                }

                rms = (float)Math.sqrt(rms / samples.length);

                if(lastPeak > peak) {
                    peak = lastPeak * 0.875f;
                }

                lastPeak = peak;
                prms = rms;
                ppeak = peak;
                //System.out.println(rms + "\t" + peak);
            }
        }
    }
}
