/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import space.ko_lab.midireader.core.*;
import space.ko_lab.midireader.player.Instrument;

/**
 *
 * @author Laurens
 */
public class Mapping
{
    public static Mapping knownMaps[];
    private LinkedList<String> order;
    private String name;
    public static final Mapping none = new Mapping("none:", false);
    public static final Mapping percussion = new Mapping("percussion:", false);
    public static void loadMapList(File map)throws Exception
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(map));
            ArrayList<Mapping> found = new ArrayList<>();
            found.add(none);
            found.add(percussion);
            String line = br.readLine();
            int n = 1;
            while(line != null)
            {
                try
                {
                    found.add(new Mapping(line, false));
                    found.add(new Mapping(line, true));
                }catch(IllegalArgumentException e)
                {
                    throw new Exception("Error reading line " + n + " of " + map.getName() + ":\n" + e);
                }
                n++;   
                line = br.readLine();
            }
            knownMaps = new Mapping[found.size()];
            knownMaps = found.toArray(knownMaps);
        }catch(IOException e)
        {
            throw new Exception("Error reading file " + map.getName() + ":\n" + e);
        }
    }
    public static Mapping findMapping(String name)
    {
        for(Mapping mo:knownMaps)
        {
            if(mo.getName().equals(name))return mo;
        }
        return null;
    }
    public Mapping(String line, boolean reversed)
    {
        order = new LinkedList<>();
        String bits[] = line.split(":");
        name = bits[0];
        if(reversed) name += " [R]";
        if(bits.length == 1)return;
        String bits2[] = bits[1].split(",");
        for(String s:bits2)
        {
            Instrument.findInstrument(s);//IllegalArgument is thrown to caller of constructor
            if(reversed)order.addFirst(s);
            else order.addLast(s);
        }
    }
    public boolean mapNote(Note note)
    {
        for(String test:order)
        {
            if(Instrument.findNote(test, note))return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String getName()
    {
        return name;
    }
    public boolean isPercussion()
    {
        return name.equals("percussion");
    }
    
}
