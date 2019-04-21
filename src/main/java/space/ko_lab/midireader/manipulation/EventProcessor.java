/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

import space.ko_lab.midireader.core.Event;
import space.ko_lab.midireader.core.Note;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * applies EventRules to all MIDI Events
 */
public class EventProcessor
{
    public ArrayList<Event> events = new ArrayList<>();
    public EventRule[] channelRules = new EventRule[17];
    public EventProcessor(ArrayList<Event> events, EventRule[] rules)
    {
        this.events = events;
        this.channelRules = rules;
    }
    public EventProcessor(ArrayList<Event> events)
    {
        this.events = events;
        setAllChannels(new EventRule(EventRule.ChanPrty.medium, EventRule.ChanMode.off, Double.MAX_VALUE, Mapping.none, 0));
        //setAllChannels(ChanPrty.high, ChanMode.off, Double.MAX_VALUE, Event.Mapping.printer);
    }
    private boolean isBlacklisted(Event event)
    {
        return channelRules[event.note.channel].prty == EventRule.ChanPrty.off;
    }
    public boolean isDrumNote(Note note)
    {
        return true;
    }
    //deprecated: drum mapping is now done elsewhere
    //TODO: actually do drum mapping elsewhere
    /*
    public EventRule.Mapping drumMapping(Note note)
    {
        switch(note.note)
        {
            case 35:
            case 38:
                System.out.println("mapped to A");
                return EventRule.Mapping.drumA;
            case 36:
            case 40:
            //case 59://sandstorm
                System.out.println("mapped to B");
                return EventRule.Mapping.drumB;
            default:
                System.out.println("no drum mapping for note " + note.note);
                return EventRule.Mapping.drums;
        }
    }
    */
    public final void setAllChannels(EventRule rule)
    {
        int i = 0;
        while(i != 17)
        {
            setChannel(i, rule);
            i++;
        }
    }
    public final void setChannel(String channel, EventRule rule)
    {
        int i = 0;
        while(i != Note.channelNames.length)
        {
            if(Note.channelNames[i].equals(channel))
            {
                setChannel(i, rule);//don't exit, there might be multiple
            }
            i++;
        }
    }
    public final void setChannel(int channel, EventRule rule)
    {
        channelRules[channel] = rule;
    }
    
    
    
    
    public ArrayList<Event> process()
    {
        double time = 0.0;
        ArrayList<Event> newEvents = new ArrayList<>();
        ArrayList<Event> currentEvents = new ArrayList<>();//events currently playing
        //events.sort(null);
        Collections.sort(events);
        
        for(Event oldEvent:events)//max length check and remove blacklisted events
        {
            /////////////////
            //octave shifts//
            /////////////////
            oldEvent.note.note += channelRules[oldEvent.note.channel].noteShift;
            double delta = oldEvent.time - time;
            time += delta;
            if(delta > 0.000001)//non-instant event, currently in a state for a while
            {
                //////////////////////////
                //simplification: max1&2//
                //////////////////////////
                int i = 0;
                while(i != 17)//for each channel, check active notes (and disable unneeded notes)
                {
                    ArrayList<Event> currentChannel = new ArrayList<>();
                    EventRule.ChanMode channelMode = channelRules[i].mode;
                    for(Event event:currentEvents)//find all events
                    {
                        if(event.note.channel == i)currentChannel.add(event);
                    }
                    //System.out.println("checking channel " + i + " with " + currentChannel.size());
                    if(channelMode == EventRule.ChanMode.max1 && currentChannel.size() > 1)//over limit max1
                    {
                        //System.out.println("max1 limiting");
                        Event.compareNotes = true;
                        //sort by note values
                        Collections.sort(currentChannel);
                        Event.compareNotes = false;
                        Event median = currentChannel.get(currentChannel.size()/2);//median-ish
                        
                        currentEvents.removeAll(currentChannel);//remove all from this channel...
                        currentEvents.add(median);//except this one
                    }
                    else if(channelMode == EventRule.ChanMode.max2 && currentChannel.size() > 2)//over limit max2
                    {
                        //System.out.println("max2 limiting");
                        Event.compareNotes = true;
                        //sort by note values
                        Collections.sort(currentChannel);
                        Event.compareNotes = false;
                        Event min = currentChannel.get(0);
                        Event max = currentChannel.get(currentChannel.size() - 1);
                        currentEvents.removeAll(currentChannel);//remove all from this channel...
                        currentEvents.add(min);//except these
                        currentEvents.add(max);
                    }
                    else if(channelMode == EventRule.ChanMode.max3 && currentChannel.size() > 2)//over limit max2
                    {
                        //System.out.println("max2 limiting");
                        Event.compareNotes = true;
                        //sort by note values
                        Collections.sort(currentChannel);
                        Event.compareNotes = false;
                        Event min = currentChannel.get(0);
                        Event max = currentChannel.get(currentChannel.size() - 1);
                        Event median = currentChannel.get(currentChannel.size()/2);//median-ish
                        currentEvents.removeAll(currentChannel);//remove all from this channel...
                        currentEvents.add(min);//except these
                        currentEvents.add(max);
                        currentEvents.add(median);
                    }                    
                    i++;
                }
                /////////////////
                //note priority//
                /////////////////
                ArrayList<Event> currentEventsFinal = new ArrayList<>();
                int printerCount = 0;
                int floppyCount = 0;
                for(Event event:currentEvents)//find all high priority events
                {
                    if(channelRules[event.note.channel].prty == EventRule.ChanPrty.high)
                    {
                        event.mapping = channelRules[event.note.channel].mapping;
                        currentEventsFinal.add(event);//add these first
                    }
                }
                
                Collections.sort(currentEvents);
                for(Event event:currentEvents)//find all medium priority events
                {
                    if(channelRules[event.note.channel].prty == EventRule.ChanPrty.medium)
                    {
                        //used to only add notes if space is available, this is now done elsewhere
                        if(channelRules[event.note.channel].mapping.isPercussion())//add all drums
                        {
                            event.mapping = Mapping.percussion;
                            currentEventsFinal.add(event);
                        }else
                        {
                            event.mapping = channelRules[event.note.channel].mapping;
                            currentEventsFinal.add(event);
                        }
                    }
                }
                //System.out.println("current events done");
                currentEvents = currentEventsFinal;
            }
            switch(oldEvent.type)
            {                
                case noteOn:
                    ////////////////////////
                    //Blacklisted channels//
                    ////////////////////////
                    //TODO blacklisted velocity?
                    if(isBlacklisted(oldEvent) == false)
                    {
                        currentEvents.add(oldEvent);
                    }
                    break;
                case noteOff:
                    if(isBlacklisted(oldEvent))break;
                    ///////////////////////
                    //note length limiter//
                    ///////////////////////
                    Event toRemove = null;//will contain start event
                    for(Event event:currentEvents)//search for start event
                    {
                        if(event.note.equals(oldEvent.note))
                        {
                            toRemove = event;
                        }
                    }
                    if(toRemove != null)//only if note was played already
                    {
                        currentEvents.remove(toRemove);//remove it from current notes
                        //toRemove: start
                        //e: stop
                        if(oldEvent.time - toRemove.time > channelRules[oldEvent.note.channel].maxNoteLen)//longer note than allowed
                        {
                            oldEvent.time = toRemove.time + channelRules[oldEvent.note.channel].maxNoteLen;//set to max note length
                            //oldEvent.mapping = toRemove.mapping;
                            newEvents.add(toRemove);//add original start event
                            newEvents.add(oldEvent);//add premature stop event
                            
                        }
                        else//allowed length
                        {
                            //oldEvent.mapping = toRemove.mapping;
                            newEvents.add(toRemove);//add original start event
                            newEvents.add(oldEvent);//add stop event
                            
                        }
                    }
                    break;
            }
        }
        Collections.sort(newEvents);
        events = newEvents;
        return newEvents;
    }
    
}
