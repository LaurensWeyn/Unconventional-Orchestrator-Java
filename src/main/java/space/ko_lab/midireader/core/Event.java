/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.core;

import java.text.DecimalFormat;
import space.ko_lab.midireader.manipulation.Mapping;

/**
 *
 * describes a MIDI event, or when notes are played and stopped.
 */
public class Event implements Comparable
{
    public double time;//time of event, in seconds past start
    public Note note;
    public NoteType type;
    public Mapping mapping;
    public static final int maxFloppies = 2;
    public static final int maxPrinters = 3;
    public Event(double time, Note note, NoteType type)
    {
        this.time = time;
        this.note = note;
        this.type = type;
    }
    @Override
    public String toString()
    {
        return type + "@" + new DecimalFormat("#.##").format(time) + ": " + note;
    }
    public static boolean compareNotes = false;
    @Override
    public int compareTo(Object o)
    {
        //System.out.println("comparing " + compareNotes);
        Event e = (Event)o;//+: this later. -: this first
        if(compareNotes)return this.note.compareTo(e.note);
        /*
        double compare = this.time - e.time;
        if(compare > -0.00001)
        {
            //prefer noteOffs before noteOns
            if(this.type == NoteType.noteOn && e.type == NoteType.noteOff)
            {
                return 1;
            }
            else if(this.type == NoteType.noteOff && e.type == NoteType.noteOn)
            {
                return -1;
            }
            else return 0;//equal
        }
        //*/
        return Double.compare(this.time, e.time);
    }

    
    
    public enum NoteType
    {
        noteOn,
        noteOff,
    }
    
}
