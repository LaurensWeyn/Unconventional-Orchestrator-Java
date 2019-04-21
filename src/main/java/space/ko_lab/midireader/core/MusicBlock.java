/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.core;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

/**
 *
 * an alternative to Events, each MusicBlock object stores when a note both starts and ends.
 */
public class MusicBlock implements Comparable
{
    public final static DecimalFormat df = new DecimalFormat("#.##");
    public double start = 0;
    public double end = 0;
    public Note note;
    public static int width = 10;
    public static double lenMult = 60;
    public static int startOffset = 0;
    public static int topPos = 0;
    public MusicBlock(double start, double end, Note note)
    {
        this.start = start;
        this.end = end;
        this.note = note;
    }
    
    public MusicBlock(Note note)
    {
        this.note = note;
    }
    public MusicBlock setStart(double start)
    {
        this.start = start;
        return this;
    }
    public MusicBlock setEnd(double end)
    {
        this.end = end;
        return this;
    }
    public void render(BufferedImage img)
    {
        //TODO use Graphics object for this instead
        int y = (int)(start * lenMult);
        int yend = (int)(end * lenMult);
        int col = note.getColour();
        int off = (note.note - startOffset) * width;//calc x offset
        while(y != yend)
        {
            int x = 1;
            while(x != width)
            {
                //System.out.println("y: " + y + " max: " + topPos);
                img.setRGB(x + off, topPos - y, col);
                
                x++;
            }
            y++;
        }
    }
    
    @Override
    public String toString()
    {
        //String out = "";
        //out += "\nStart time: "+start;
        //out += "\nEnd time: " + end;
        //out += "\nNote/channel: " + note + "/" + channel;
        //return out + "\n";
        return "[" + df.format(start) + "/" + df.format(end) + " : " + note + "]";
    }
    public boolean isSame(MusicBlock b)
    {
        return b.note.equals(this.note);
    }

    @Override
    public int compareTo(Object o)
    {
        MusicBlock m = (MusicBlock)o;
        return Integer.compare(m.note.channel, this.note.channel);
    }
}
