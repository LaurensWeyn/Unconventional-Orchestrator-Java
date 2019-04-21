/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

import java.util.ArrayList;
import space.ko_lab.midireader.core.Event;
import space.ko_lab.midireader.core.Note;

/**
 *
 * @author Laurens
 */
public class DrumKit
{
    public ArrayList<DrumMapping> drums;
    public DrumKit()
    {
        drums = new ArrayList<>();
    }
    public DrumKit(ArrayList<DrumMapping> drums)
    {
        this.drums = drums;
    }
    public DrumKit(String line)
    {
        System.out.println("init drumKit from line " + line);
        drums = new ArrayList<>();
        if(line.equals(""))
        {
            
            return;
        }
        String bits[] = line.split(",");
        for(String s:bits)
        {
            String subBits[] = s.split("-");
            drums.add(new DrumMapping(Integer.parseInt(subBits[0]), Integer.parseInt(subBits[1])));
        }
    }
    public String toLine()
    {
        DrumKit write = cleanup();
        String line = null;
        for(DrumMapping d:write.drums)
        {
            if(line == null)line = d.toLine();
            else line += "," + d.toLine();
        }
        return line;
    }
    public DrumKit cleanup()
    {
        ArrayList<DrumMapping> cleanMappings = new ArrayList<>();
        for(DrumMapping dm:drums)
        {
            if(dm.qty > 0)cleanMappings.add(dm);
        }
        return new DrumKit(cleanMappings);
    }
    public int getDrumID(Note n)
    {
        for(DrumMapping dm:drums)
        {
            if(dm.noteVal == n.note)return dm.drumID;
        }
        return 0;
    }
    public void findDrums(ArrayList<Event> events)
    {
        cleanup();
        for(DrumMapping dm:drums)
        {
            dm.qty = 0;//we'll re-tally the drums now
        }
        for(Event e:events)
        {
            if(e.mapping !=null && e.mapping.isPercussion() && e.type == Event.NoteType.noteOn)//relevant
            {
                boolean found = false;
                for(DrumMapping dm:drums)
                {
                    if(dm.noteVal == e.note.note)
                    {
                        dm.addQty();//new entry of this drum found
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    //new drum mapping found
                    drums.add(new DrumMapping(e.note.note));
                }
            }
        }
    }
}
