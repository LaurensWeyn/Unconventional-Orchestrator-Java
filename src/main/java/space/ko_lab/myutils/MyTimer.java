/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myutils;

/**
 *
 * @author Laurens
 */
public class MyTimer
{
    private final long startTime;
    public MyTimer()
    {
        startTime = System.nanoTime();
    }
    public long getTimestamp()
    {
        return startTime;
    }
    public long getSeconds()
    {
        return (System.nanoTime()- startTime) / 1000000000;
    }
    public long getMilliseconds()
    {
        return (System.nanoTime()- startTime) / 1000000;
    }
    public long getNanoseconds()
    {
        return (System.nanoTime()- startTime);
    }
    @Override
    public String toString()
    {
        return getSeconds() + ":" + (getMilliseconds() % 1000) + ":" + (getNanoseconds() % 1000);
    }
    
    public boolean hasPassedSec(long seconds)
    {
        return getSeconds() > seconds;
    }
    public boolean hasPassedMil(long milliseconds)
    {
        return getMilliseconds() > milliseconds;
    }
    public boolean hasPassedNan(long nanoseconds)
    {
        return getNanoseconds() > nanoseconds;
    }
    
    public static void waitMs(long time)
    {
        if(time <= 0)return;
        try
        {
            Thread.sleep(time);
        }catch(InterruptedException e)
        {
            
        }
    }
}
