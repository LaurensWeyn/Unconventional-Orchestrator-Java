/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

import space.ko_lab.midireader.core.Note;



/**
 *
 * stores information on how a specific channel of notes should be handled
 */
public class EventRule
{
    public ChanPrty prty = ChanPrty.medium;
    public ChanMode mode = ChanMode.off;
    public Mapping mapping = Mapping.none;
    public double maxNoteLen =Double.MAX_VALUE;
    public int noteShift = 0;

    public EventRule()
    {
    }
    
    public EventRule(int channel)
    {
        setRender(channel);
    }
    public EventRule(String line)
    {
        String bits[] = line.split(",");
        this.prty = ChanPrty.valueOf(bits[0]);
        this.mode = ChanMode.valueOf(bits[1]);
        this.maxNoteLen = Double.parseDouble(bits[2]);
        this.mapping = Mapping.findMapping(bits[3]);
        this.noteShift = Integer.parseInt(bits[4]);
    }
    public String toLine()
    {
        String line = "";
        line += prty + ",";
        line += mode + ",";
        line += maxNoteLen + ",";
        line += mapping + ",";
        line += noteShift + "";
        return line;
    }
    public EventRule(ChanPrty priority, ChanMode mode, double maxNoteLength, Mapping mapping, int noteShift)
    {
        this.prty = priority;
        this.mode = mode;
        this.maxNoteLen = maxNoteLength;
        this.mapping = mapping;
        this.noteShift = noteShift;
    }

    private String tag(String text,String tag)
    {
        return "<"+tag+">"+text+"</"+tag+">";
    }
    private int channel = -1;
    public void setRender(int channel)
    {
        this.channel = channel;
    }
    
    @Override
    public String toString()
    {
        if(channel == -1)return "TODO: string output here";
        //TODO: allow custom channel names
        String chString = Note.channelNames[channel];
        String chColour = Note.channelColours[channel];
        String output = channel + " " + mapping + ": ";
        if(chString != null && !chString.equals(""))
        {
            output += "(" + chString + ") ";
        }
        if(chColour != null)
        {
            output += chColour;
        }
        else
        {
            output += "Brown";
        }
        if(mapping.equals(Mapping.none) == false)output = tag(output, "b");
        return tag(output, "html");
    }
    
    public enum ChanPrty
    {
        medium,
        high,
        low,
        off,
    }
    public enum ChanMode
    {
        off,
        max1,
        max2,
        max3,
        latest,//broken!
    }
}
