/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;


/**
 *
 * generates and stores MusicBlocks from a set of events,
 * eliminating duplicate noteOn and noteOff events
 */
public final class BlockSet
{
    public ArrayList<MusicBlock> doneBlocks = new ArrayList<>();
    private ArrayList<MusicBlock> WIPBlocks = new ArrayList<>();
    public ArrayList<Event> validEvents = new ArrayList<>();
    public BlockSet()
    {
        
    }
    public BlockSet(ArrayList<Event> events)
    {
        for(Event e:events)
        {
            addBlock(e);
        }
    }
    public static ArrayList<Event> cleanup(ArrayList<Event> input)
    {
        return new BlockSet(input).validEvents;
    }
    public void addBlock(Event event)
    {
        switch(event.type)
        {
            case noteOn:
                for(MusicBlock b:WIPBlocks)
                {
                    if(b.note.equals(event.note))
                    {
                        //System.out.println("WARNING: note " + event +" is already on!");
                        return;
                    }
                }
                WIPBlocks.add(new MusicBlock(event.note).setStart(event.time));
                validEvents.add(event);
                break;
            case noteOff:
                MusicBlock toRemove = null;
                //System.out.println("WIPBlocks size: "+WIPBlocks.size());
                for(MusicBlock b:WIPBlocks)
                {
                    if(b.note.equals(event.note))
                    {

                        doneBlocks.add(new MusicBlock(event.note).setEnd(event.time).setStart(b.start));//event includes end time
                        validEvents.add(event);
                        toRemove = b;
                        break;//no need to keep searching
                    }
                }
                if(toRemove !=null)
                {
                    WIPBlocks.remove(toRemove);
                    return;
                }
                //else System.out.println("WARNING: note " + event +" isn't on yet!");
                break;
        }
    }
    public int minNote = Integer.MAX_VALUE;
    public int maxNote = Integer.MIN_VALUE;
    public double songLen = 0.0;
    public double songNotes = 0;
    public void calcStats()
    {
        minNote = Integer.MAX_VALUE;
        maxNote = Integer.MIN_VALUE;
        songLen = 0.0;
        songNotes = 0;
        for(MusicBlock b:doneBlocks)
        {
            //System.out.println(b);
            if(b.note.note > maxNote)maxNote = b.note.note;
            if(b.note.note < minNote)minNote = b.note.note;
            if(b.end > songLen) songLen = b.end;
            songNotes++;
        }
    }
    public BufferedImage genImage()
    {
        calcStats();
        //double length = ((double)songLen * 60.0) / ((double)division * ((60000000.0 / (double)tempo)));
        //double length = (double)songLen / ((double)division * (double)tempo);
        //System.out.println("song length: " + songLen);
        
        Collections.sort(doneBlocks);//sort by note channel/colour
        System.out.println("generating image...");
        int width = (maxNote - minNote + 1) * MusicBlock.width;
        int height = (int)(songLen * MusicBlock.lenMult);
        System.out.println("width: " + width);
        System.out.println("height: " + height);
        BufferedImage img = new BufferedImage(width, height + 1, BufferedImage.TYPE_INT_RGB);
        //TODO do this with graphics!
        System.out.println("generating backdrop...");
        
        int x = 0;
        while(x != width)
        {
            int colour = 0xFFFFFF;//default white
            switch(((x/MusicBlock.width) + minNote) % 12)
            {
                case 1:
                case 3:
                case 6:
                case 8:
                case 10:
                    colour = 0xE6E6E6;//grey for black notes
                    break;
            }
            if(x % MusicBlock.width == 0)colour = 0xC0C0C0;//seperator lines
            
            int y = 0;
            while(y != height)
            {
                img.setRGB(x, y, colour);
                y++;
            }
            x++;
        }
        MusicBlock.startOffset = minNote;
        MusicBlock.topPos = height;
        int i = 0;
        System.out.println("rendering notes... ("+doneBlocks.size()+")");
        for(MusicBlock b:doneBlocks)
        {
            //System.out.println("rendering " + i + " of " +num);
            b.render(img);
            i++;
        }
        return img;
    }

    public ArrayList<MusicBlock> getBlocks()
    {
        return doneBlocks;
    }

    public ArrayList<Event> getEvents()
    {
        return validEvents;
    }
    
    
}
