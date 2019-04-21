/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

import java.io.File;
import java.io.FileWriter;

/**
 *
 * holds rules for all channels in a MIDI file and additional configuration data
 */
public class RuleSet
{
    EventRule[] noteStatus = new EventRule[17];
    DrumKit drumKit;
    boolean forceRange = false;
    boolean trackAsChannel = false;
    public RuleSet(File file)
    {
        
    }
    private String boolAsString(boolean b)
    {
        return b?"True":"False";
    }
    public boolean StringAsBool(String s)
    {
        if(s.equalsIgnoreCase("true")||s.equalsIgnoreCase("yes"))return true;
        else return false;
    }
    public void save(File file)
    {
        try(FileWriter fr = new FileWriter(file))
        {
            String output = "";
            for(EventRule note:noteStatus)
            {
                output += note.toLine() + "\n";
            }
            output += boolAsString(trackAsChannel) + "\n";
            output += drumKit.toLine();
            fr.write(output);
        }catch(Exception e)
        {
            System.out.println("error while saving config:");
            e.printStackTrace();
        }
    }
}
