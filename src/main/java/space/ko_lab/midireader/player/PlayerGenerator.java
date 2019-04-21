/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

import java.util.ArrayList;
import java.util.Collections;
import space.ko_lab.midireader.core.Event;
import space.ko_lab.midireader.manipulation.DrumKit;

/**
 *
 * @author Laurens
 */
public class PlayerGenerator implements Runnable
{
    public ArrayList<Event> events;
    public String text;
    public int notesPlayed = 0;
    public boolean stopPlaying = false;
    public static boolean playNotes = true;
    public DrumKit drumKit;
    public PlayerGenerator(ArrayList<Event> events, DrumKit drumKit, String text)
    {
        Collections.sort(events);
        this.events = events;
        this.text = text;
        this.drumKit = drumKit;
    }
    public PlayerGenerator(ArrayList<Event> events, DrumKit drumKit)
    {
        Collections.sort(events);
        this.events = events;
        this.drumKit = drumKit;
    }
    private long tAsLong(double time)
    {
        return (long)(time * 1000000000.0);
    }
    
    public ArrayList<Event> assign(boolean realTime)
    {
        double time = 0.0;
        stopPlaying = false;
        ArrayList<Event> currentEvents = new ArrayList<>();
        ArrayList<Event> mappedEvents = (ArrayList<Event>)events.clone();
        Instrument.resetAll();
        long startTime = System.nanoTime();
        for(Event e:events)
        {
            double delta = e.time - time;
            
            
            
            if(delta > 0.01)//play notes while waiting (not if notes are to follow)
            {
                //add all events to current instruments:
                //System.out.println("checking " + currentEvents.size() + " events");
                for(Event event:currentEvents)
                {
                    if(event != null && event.mapping != null)
                    {
                        if(event.mapping.isPercussion() == false && event.mapping.mapNote(event.note) == false)
                        {
                            mappedEvents.remove(event);//this event won't be mapped, of course
                        }else if(event.mapping.isPercussion())
                        {
                            for(Instrument i:Instrument.list)i.playDrum(tAsLong(time), drumKit.getDrumID(event.note));
                        }
                    }
                }
                //commit all notes:
                for(Instrument i:Instrument.list)i.commit(tAsLong(time), delta);
                time += delta;
                if(realTime)while(tAsLong(time) > System.nanoTime() - startTime)
                {
                    //wait for the right time
                    try
                    {
                        Thread.sleep(1);
                    }catch(InterruptedException err){}
                    if(stopPlaying)//deinit and stop (set by other thread)
                    {
                        Instrument.resetAll();
                        return mappedEvents;
                    }
                }
                
            }else time += delta;//keep accurate time (if applicable
            //System.out.println("now reading note " + e);
            //see what this new note changes:
            switch(e.type)
            {
                
                case noteOn:
                    notesPlayed++;
                    currentEvents.add(e);
                    //TODO: deal with percussion and notesPlayed count etc.
                    
                    //if(e.mapping == EventRule.Mapping.printer)currentPrinter.add(e.note);
                    //else if(e.mapping == EventRule.Mapping.floppy)currentFloppy.add(e.note);
                    //else if(e.mapping == EventRule.Mapping.drumA)fc.drum(tAsLong(time), 0);
                    //else if(e.mapping == EventRule.Mapping.drumB)fc.drum(tAsLong(time), 1);
                    //else notesPlayed--;//undo if not actually played                    
                    break;
                case noteOff:
                    int i = 0;
                    while(i != currentEvents.size())
                    {
                        if(currentEvents.get(i).note.equals(e.note))
                        {
                            currentEvents.remove(i);
                            break;
                        }
                        i++;
                    }
                    //if(e.mapping == EventRule.Mapping.printer)currentPrinter.remove(e.note);
                    //else if(e.mapping == EventRule.Mapping.floppy)currentFloppy.remove(e.note);
                    //else if(e.mapping == EventRule.Mapping.drums);//ignore drum off commands
                    break;
            }
            
            
            
        }
        //commit stop to ensure all is silent:
        for(Instrument i:Instrument.list)i.commit(tAsLong(time), 0);
        return mappedEvents;
    }
    @Override
    public void run()
    {
        try
        {
            assign(true);
        } catch (Exception e)
        {
            System.out.println("error while playing song:");
            e.printStackTrace();
        }
    }
}
