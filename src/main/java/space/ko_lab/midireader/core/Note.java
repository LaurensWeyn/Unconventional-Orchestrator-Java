/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.core;

/**
 *
 * Stores information about a specific tone
 */
public class Note implements Comparable
{
    public int note;
    public int channel;
    public int velocity;
    public final static int notesInOctave = 12;
    public static String channelNames[] = new String[17];
    public static String channelColours[] = new String[17];
    public static void setColours()
    {
            channelColours[0] = "dark red";
            channelColours[1] = "bright red";
            channelColours[2] = "ew yellow";
            channelColours[3] = "dark purple";
            channelColours[4] = "dark green";
            channelColours[5] = "bright green";
            channelColours[6] = "blue";
            channelColours[7] = "light blue";
            channelColours[8] = "pink purple";
            channelColours[9] = "black";
            channelColours[10] ="grey";
    }
    public int getColour()//channel->colour lookup. TODO improve this?
    {
        switch(channel)
        {
            case 0: return 0x800000;//dark red
            case 1: return 0xFF0000;//bright red
            case 2: return 0x808000;//ew yellow
            case 3: return 0x551A8B;//dark purple
            case 4: return 0x008000;//dark green
            case 5: return 0x00FF00;//bright green
            case 6: return 0x008080;//lightish blue
            case 7: return 0x00FFFF;//bright light blue
            case 8: return 0x800080;//pink purple
            case 9: return 0x000000;//black
            case 10:return 0x808080;//grey
            //case 5:return 0x;
                        
                        
            default: return 0xB8860B;//brown
        }
    }
    public Note(Note note)
    {
        this.channel = note.channel;
        this.note = note.note;
        this.velocity = note.velocity;
    }
    public Note(int note)
    {
        this.note = note;
        this.channel = 0;
        this.velocity = 255;
    }
    public Note(int note, int channel)
    {
        this.note = note;
        this.channel = channel;
        this.velocity = 255;
    }
    public Note(int note, int channel, int velocity)
    {
        this.note = note;
        this.channel = channel;
        this.velocity = velocity;
    }
    public double getFreq()
    {
        //TODO include pitch bend?
        return 440.0 * Math.pow(2.0,(double) ((double)note - 69.0) / 12.0);
    }
    
    @Override
    public String toString()
    {
        String chString = channelNames[channel];
        if(chString == null) chString = "";
        chString += "ch." + channel;
        return note +"-" + chString;
    }
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + this.note;
        hash = 11 * hash + this.channel;
        return hash;
    }
    @Override
    public int compareTo(Object o)
    {
        return ((Note)o).note - this.note;
    }
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Note other = (Note) obj;
        if (this.note != other.note)
        {
            return false;
        }
        if (this.channel != other.channel)
        {
            return false;
        }
        return true;
    }

    
}
