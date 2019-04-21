/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

import jssc.SerialPortException;

/**
 *
 * @author Laurens
 */
public class LocalPlayer extends Player
{
    @Override
    public void init()throws Exception
    {
        for(Instrument i:Instrument.list)
        {
            i.initSerial(115200);
        }
        System.out.println("resetting");
        reset();
    }
    @Override
    public void play()
    {
        play(System.nanoTime());
    }
    
    public void play(final long sync)
    {
        //final long sync = System.nanoTime();
        //PLAY AT ONCE!
        for(final Instrument i:Instrument.list)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        i.play(sync);
                    }
                    catch(SerialPortException | InterruptedException e)
                    {
                        
                    }
                }
          }.start();
        }
    }
    @Override
    public void stop()
    {
        for(Instrument i: Instrument.list)
        {
            i.stopPlaying();
        }
    }
    @Override
    public boolean isPlaying()
    {
        for(Instrument i: Instrument.list)
        {
            if(i.isPlaying())return true;
        }
        return false;//nothing is playing
    }

    @Override
    public void reset()throws Exception
    {
        for(Instrument i:Instrument.list)
        {
            i.startup();
        }
    }
}
