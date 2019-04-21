/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

import java.util.ArrayList;
import space.ko_lab.midireader.core.BlockSet;
import space.ko_lab.midireader.core.Event;
import space.ko_lab.midireader.core.Note;

/**
 *
 * @author Laurens
 */
public class Manipulator
{
    public Mode mode;
    public Criteria criteria;
    public boolean inverted;
    public int channel, destination;
    public double min, max;

    public Manipulator(Mode mode, Criteria criteria, boolean inverted, int channel, int destination, double min, double max)
    {
        this.mode = mode;
        this.criteria = criteria;
        this.inverted = inverted;
        this.channel = channel;
        this.destination = destination;
        this.min = min;
        this.max = max;
    }
    public Manipulator(String line)
    {
        System.out.println("init manipulator from line " + line);
        String bits[] = line.split("-");
        channel = Integer.parseInt(bits[0]);
        destination = Integer.parseInt(bits[1]);
        min = Double.parseDouble(bits[2]);
        max = Double.parseDouble(bits[3]);
        inverted = bits[4].equals("true");
        mode = Mode.valueOf(bits[5]);
        criteria = Criteria.valueOf(bits[6]);
    }
    public String toLine()
    {
        String line = "";
        line += channel + "-";
        line += destination + "-";
        line += min + "-";
        line += max + "-";
        line += inverted?"true":"false" + "-";
        line += mode + "-";
        line += criteria;
        return line;
    }

    @Override
    public String toString()
    {
        return channel + " --> " + (inverted?"!":"") + mode + " " + criteria + "[" + min + "-" + max + "] --> " + destination;
    }
    public static BlockSet Manipulate(ArrayList<Manipulator> mods, ArrayList<Event> events)
    {
        ArrayList<Event> newEvents = (ArrayList<Event>)events.clone();
        for(Manipulator m:mods)
        {
            newEvents = m.manipulate(newEvents);
        }
        System.out.println("newEvents: " + newEvents);
        return new BlockSet(newEvents);
    }
    public ArrayList<Event> manipulate(ArrayList<Event> events)
    {
        ArrayList<Event> newEvents = new ArrayList<>();
        for(Event e:events)
        {
            if(meetsCriteria(e))
            {
                Event destMap = new Event(e.time, new Note(e.note), e.type);
                destMap.note.channel = destination;
                switch(mode)
                {

                    case copy:
                        newEvents.add(e);
                        newEvents.add(destMap);
                        break;
                    case delete:
                        break;
                    case move:
                        newEvents.add(destMap);
                }
            }else
            {
                newEvents.add(e);
            }
        }
        return newEvents;
    }
    public boolean meetsCriteria(Event event)
    {
        Note note = event.note;
        if(note.channel != channel)return false;//irrelevant channel
        boolean output = false;
        int minInt = (int)min, maxInt = (int)max;
        switch(criteria)
        {
            case notePitch:
                output = note.note >= minInt && note.note <= maxInt;
                break;
            case noteVelocity:
                output = note.velocity >= minInt && note.velocity <= maxInt;
                break;
            case timeFrame:
                output = event.time >= min && event.time <= max;
                break;
        }
        if(inverted)output = !output;
        return output;
    }
    public enum Criteria
    {
        timeFrame,
        notePitch,
        noteVelocity
    }
    public enum Mode
    {
        delete,
        copy,
        move,
    }
}
