/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

import space.ko_lab.myutils.SerialMessage;
import jssc.SerialPortException;
import space.ko_lab.midireader.core.Note;

/**
 *
 * @author Laurens
 */
public class FloppyControl extends Instrument
{
    private final int numDrives = 3;
    //public Note currentNote[] = new Note[numDrives];
    private long lastDrum[] = new long[numDrives];

    public FloppyControl(String configLine)
    {
        super(configLine);
        //TODO take in drum data
    }
    public boolean drumPlaying(int id)
    {
        //50ms
        return lastDrum[id] + 50000000 > System.nanoTime();
    }
    public void noNote(long delta)
    {
        //turn off ALL drives
        int i = 0;
        while(i != numDrives)
        {
            noNote(delta, i);
            i++;
        }
    }
    public void noNote(long delta, int drive)
    {
        /*if(PlayerGenerator.playNotes == false || currentNote[drive] == null)
        {
            return;//no need to re-send note stop
        }*/
        messages.add(new SerialMessage(delta, (byte)(26 + drive)));//drive X off
        //currentNote[drive] = null;
    }
    /*public void playSet(long delta, ArrayList<Note> events)
    {
        Note[] newNotes = new Note[currentNote.length];
        ArrayList<Note> oldEvents = new ArrayList<>();
        //step 1: copy over relevant new notes
        int i = 0;
        while(i!= currentNote.length)
        {
            if(events.contains(currentNote[i]))
            {
                newNotes[i] = currentNote[i];
                events.remove(currentNote[i]);//no longer worry about playing it
                oldEvents.add(currentNote[i]);
            }
            i++;
        }
        //step 2: fill in new notes (looping over events)
        i = 0;
        int newPos = 0;
        while(i != events.size())
        {
            //find free space
            while(newNotes[newPos] != null)
            {
                newPos++;
                if(newPos > newNotes.length)
                {
                    System.out.println("WARN: too many notes for floppy!");
                    break;
                }
            }
            newNotes[newPos] = events.get(i);//assign it a note
            i++;
        }
        //add those notes back or we'll permanently remove them
        events.addAll(oldEvents);
        //step 3: play new notes (it won't send notes already active)
        i = 0;
        while(i != newNotes.length)
        {
            if(newNotes[i] == null)
            {
                noNote(delta, i);
            }
            else
            {
                playNote(delta, i, newNotes[i]);
            }
            i++;
        }
    }*/
    public void playNote(long delta, int drive, Note note)
    {
        if(drive > channels)return;
        /*if(PlayerGenerator.playNotes == false || (currentNote[drive] != null && currentNote[drive].equals(note)))
        {
            return;//no need to re-send note
        }*/
        if(note == null)noNote(delta, drive);
        else
        {
            int period = (int)((double)(1.0/note.getFreq()) * 1000000.0);
            byte toSend[] = new byte[4];
            toSend[0] = (byte)((period >> 14)&(byte)0xFF | (byte)0b10000000);
            toSend[1] = (byte)((period >> 7 )&(byte)0xFF | (byte)0b10000000);
            toSend[2] = (byte)((period      )&(byte)0xFF | (byte)0b10000000);
            toSend[3] = (byte) drive;
            messages.add(new SerialMessage(delta, toSend));
            //currentNote[drive] = note;
        }
    }
    @Override
    public void playDrum(long delta, int drum)
    {
        lastDrum[drum] = System.nanoTime();
        if(PlayerGenerator.playNotes == false || drum == 0)return;
        messages.add(new SerialMessage(delta, (byte)(50 + drum)));//was 51
    }

    @Override
    public void playSet(long delta, Note[] notes, double time)
    {
        Note[] oldNotes = getPlayingNotes();
        int i = 0;
        for(Note n:notes)
        {
            if(n == null && oldNotes[i] == null)//note has changed
            {
                
            }else if(n == null)
            {
                noNote(delta, i);//now off
            }else if (!n.equals(oldNotes[i]))
            {
                playNote(delta, i, n);//note changed
            }
            i++;
        }
    }


    @Override
    public void startup()throws SerialPortException
    {
        //silence all drives:
        int i = 0;
        while(i != channels)
        {
            port.writeInt(26 + i);//does this work? no, no it does not. now?
            i++;
        }
    }
}
