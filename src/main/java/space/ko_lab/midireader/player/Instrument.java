/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

import space.ko_lab.myutils.SerialPack;
import space.ko_lab.myutils.SerialMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import jssc.SerialPort;
import jssc.SerialPortException;
import space.ko_lab.midireader.core.Note;

/**
 *
 * @author Laurens
 */
public abstract class Instrument
{
    public static ArrayList<Instrument> list = new ArrayList<>();
    protected SerialPack messages;
    protected int channels;
    protected int drums = 0;
    protected String portName;
    protected char code;
    
    private boolean interrupt = false;
    private boolean playing = false;
    //internal:
    private Note[] oldNotes;
    private Note[] notes;
    
    protected String extraOptions;
    protected boolean transmit;
    protected SerialPort port;
    
    public static Instrument generateInstrument(String line)
    {
        String bits[] = line.split(":");
        switch(bits[0].toLowerCase())
        {
            case "cnc": return new PrinterControl(bits[1]);
            case "uic": return new FloppyControl(bits[1]);
            default: return null;
        }
    }
    protected static int getCodeChannel(String mappingCode)
    {
        return Integer.parseInt(mappingCode.substring(1)) - 1;
    }
    public static Instrument findInstrument(String mappingCode)
    {
        char code = mappingCode.charAt(0);
        int channel = getCodeChannel(mappingCode);
        for(Instrument i:list)
        {
            if(i.getCode() == code)
            {
                if(i.getChannels() < channel)throw new IllegalArgumentException("channel " + channel + " not available on instrument " + code);
                return i;
            }
        }
        throw new IllegalArgumentException("instrument code " + code + " not found");
    }
    public static void loadInstruments(File file)throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        while(line != null)
        {
            list.add(generateInstrument(line));
            line = br.readLine();
        }
    }
    public static boolean findNote(String mappingCode, Note note)
    {
        Instrument i = findInstrument(mappingCode);
        if(i.isNoteSet(mappingCode))return false;
        if(note != null)i.setNote(mappingCode, note);
        return true;
    }
    public Instrument(String configLine)
    {
        String bits[] = configLine.split(",");
        code = bits[0].charAt(0);
        portName = bits[1];
        channels = Integer.parseInt(bits[2]);
        extraOptions = bits[3];
        messages = new SerialPack();
        notes = new Note[channels];
        oldNotes = new Note[channels];
    }
    public static void resetAll()
    {
        for(Instrument i:list)
        {
            i.commit(0, 0);
            i.clear();
        }
    }
    public Instrument(String configLine, SerialPack messages)
    {
        this(configLine);
        this.messages = messages;
    }
    public void setNote(String mappingCode, Note note)
    {
        notes[getCodeChannel(mappingCode)] = note;
    }
    public boolean isNoteSet(String mappingCode)
    {
        return notes[getCodeChannel(mappingCode)] != null;
    }
    public final void commit(long delta, double time)
    {
        playSet(delta, notes, time);
        oldNotes = notes;
        notes = new Note[channels];
    }
    public abstract void playSet(long delta, Note[] notes, double time);
    public void playDrum(long delta, int drum)
    {
        
    }
    public void clear()
    {
        messages = new SerialPack();
    }
    public void setData(SerialPack data)
    {
        messages = data;
    }
    
    public void initSerial(int baud) throws SerialPortException
    {
        port = new SerialPort(portName);
        port.openPort();
        port.setParams(baud, 8, 1, 0);
        //startup();
    }
    public abstract void startup() throws SerialPortException;
    public void deInitSerial() throws SerialPortException
    {
        port.closePort();
    }
    public void sendCmd(SerialMessage sm)throws SerialPortException
    {
        sendCmd(sm.getData());
    }
    public void sendCmd(byte... data) throws SerialPortException
    {
        port.writeBytes(data);
        //System.out.println("sent cmd");
    }
    public void stopPlaying()
    {
        interrupt = true;
    }
    public void play(long sync)throws SerialPortException, InterruptedException
    {
        System.out.println("now playing " + this);
        //System.out.println(messages.getInstructions());
        playing = true;
        interrupt = false;
        for(SerialMessage sm:messages.getInstructions())
        {
            while(sm.getDelta() > (System.nanoTime() - sync))
            {
                if(interrupt)
                {
                    startup();//reset device
                    playing = false;//no longer playing
                    return;
                }
                Thread.sleep(1);
            }
            sendCmd(sm.getData());
        }
        playing = false;
    }
    /////////////////////////////////
    //boring get methods start here//
    /////////////////////////////////
    public boolean isPlaying()
    {
        return playing;
    }
    public Note[] getPlayingNotes()
    {
        return oldNotes;
    }
    public SerialPack getData()
    {
        return messages;
    }

    public int getChannels()
    {
        return channels;
    }

    public int getDrums()
    {
        return drums;
    }

    public String getPortName()
    {
        return portName;
    }

    public char getCode()
    {
        return code;
    }
    
}
