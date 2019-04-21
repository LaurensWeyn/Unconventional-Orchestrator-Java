/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

/**
 *
 * @author Laurens
 */
public abstract class Player
{
    public abstract void init() throws Exception;
    public abstract void play();
    public abstract void stop();
    public abstract boolean isPlaying();
    public abstract void reset() throws Exception;
}
