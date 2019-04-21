/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import javax.imageio.ImageIO;
import space.ko_lab.midireader.manipulation.DrumKit;
import space.ko_lab.midireader.manipulation.EventProcessor;
import space.ko_lab.midireader.player.PlayerGenerator;

/**
 *
 * @author Laurens
 */
public class MIDIReader
{
    static BlockSet image = new BlockSet();
    public static boolean trackAsChannel = false;
    private static int byteToInt(byte b)
    {
        return (int) b & 0xFF;
    }
    private static void parseTrack(ByteBuffer data, int trackNum)
    {
        double time = 0;
        int lastEvent = 0, lastChannel = 0;
        while(data.hasRemaining())
        {
            int deltaTime = varLength(data);
            //time += deltaTime;
            time += ((double)deltaTime * 60.0) / ((double)division * ((60000000.0 / (double)tempo)));
            int eventType = byteToInt(data.get());
            if(eventType == 0xFF)//meta event
            {
                byte metaType = data.get();
                int length = varLength(data);
                switch(metaType)
                {
                    case 0:
                        int sequenceNum = data.getShort();
                        System.out.println("sequence num: " + sequenceNum);
                        break;
                    case 1://text
                    case 2://copyrights
                    case 3://track name
                    case 4://instrument name
                    case 5://lyrics
                        byte textArr[] = new byte[length];
                        data.get(textArr);
                        String text = new String(textArr);
                        System.out.println("\"" + text + "\"");
                        if(metaType == 3)
                        {
                            if(trackNum-1 > 0)
                            Note.channelNames[trackNum - 1 - 1] = text;//name the track
                        }
                        break;
                    case 81:
                        byte[] tmp = new byte[3];
                        data.get(tmp);
                        byte[] tmp2 = new byte[4];
                        tmp2[3]= tmp[2];
                        tmp2[2]= tmp[1];
                        tmp2[1]= tmp[0];
                        tempo = ByteBuffer.wrap(tmp2).getInt();
                        //tempo = byteToInt(tmp[2]) << 16 | byteToInt(tmp[1]) << 8 | byteToInt(tmp[0]);
                        System.out.println("tempo set to " +tempo);
                        System.out.println("BPM: " + (60000000.0/(double)tempo));
                        break;
                    case 47://end of track
                        System.out.println("End of track gracefully reached");
                        return;
                    default:
                        System.out.println("WARNING: unknown meta type " +metaType);
                        byte dump[] = new byte[length];
                        data.get(dump);//so next instruction won't mess up
                }
            }else//normal event
            {
                //System.out.println("full event code: " +Integer.toHexString(eventType));
                int event = (eventType & 0b11110000) >> 4;
                int channel = eventType & 0b00001111;
                int param1, param2;
                if(event < 0x8)//running status
                {
                    //System.out.println("running status");
                    param1 = eventType;//wasn't event type: it was param1
                    //recall status from last time:
                    event = lastEvent;
                    channel = lastChannel;
                }
                else
                {
                    param1 = byteToInt(data.get());
                }
                
                
                
                switch(event)
                {
                    case 0x8://note off/on
                    case 0x9:
                        param2 = byteToInt(data.get());
                        Event.NoteType noteState = Event.NoteType.noteOn;
                        if(event == 0x8)noteState = Event.NoteType.noteOff;
                        if(param2 == 0)noteState = Event.NoteType.noteOff;
                        //min velocity: (TODO improve?)
                        if(noteState == Event.NoteType.noteOn && param2 < 64)break;//ignore <64 velocity
                        
                        //System.out.println("note " + (noteState?"on  ":"off ") + param1 + " on channel " + channel + " at "+time);
                        if(trackAsChannel == true)channel = trackNum;
                        Event noteEvent = new Event(time, new Note(param1, channel, param2), noteState);
                        image.addBlock(noteEvent);
                        break;
                    case 0xA://note aftertouch (TODO)
                    case 0xB://controller (TODO)
                        param2 = byteToInt(data.get());//read param2 only for now
                        break;
                    case 0xC://program change (TODO)
                        break;
                    case 0xD://channel aftertouch (all notes) (TODO)
                        break;
                    case 0xE://pitch bend event (TODO)
                         param2 = byteToInt(data.get());//read param2 only for now
                        break;
                    default://this should never happen
                        System.out.println("WTF? event status " + Integer.toHexString(event));
                        System.out.println("param1: " + param1);
                }
                lastEvent = event;
                lastChannel = channel;
                
            }
        }
    }
    final static byte EXTFLAG = (byte)0b10000000;
    final static byte IEXTFLAG =(byte)0b01111111;
    private static int varLength(ByteBuffer buffer)
    {
        int output = 0;
        byte a = buffer.get();
        output = a & IEXTFLAG;
        //System.out.println("with a: " + Integer.toBinaryString(output));
        if((a & EXTFLAG) == EXTFLAG)
        {
            output = output << 7;
            byte b = buffer.get();
            output |= b & IEXTFLAG;
            //System.out.println("with b: " + Integer.toBinaryString(output));
            if((b & EXTFLAG) == EXTFLAG)
            {
                output = output << 7;
                byte c = buffer.get();
                output |= c & IEXTFLAG;
                //System.out.println("with c: " + Integer.toBinaryString(output));
                if((c & EXTFLAG) == EXTFLAG)
                {
                    output = output << 7;
                    byte d = buffer.get();
                    output |= d & IEXTFLAG;
                    //System.out.println("with d: " + Integer.toBinaryString(output));
                    if((d & EXTFLAG) == EXTFLAG)
                    {
                        throw new Error("varLength buffer overflow");
                    }
                }
            }
        }
        return output;
    }
    static int division = -1;
    static int tempo = 60000000 / 120;//default 120BPM (stored as microseconds per quarter-note)
    public static BlockSet parseSong(String fileName)
    {
        try
        {
            image = new BlockSet();//reset
            File file = new File(fileName);
            FileInputStream fin = new FileInputStream(file);
            byte fileContent[] = new byte[(int)file.length()];
            fin.read(fileContent);
            ByteBuffer data = ByteBuffer.wrap(fileContent);
            int headerA = data.getInt();
            int headerB = data.getInt();
            if(headerA != 0x4D546864 && headerB != 6)//"MThd", 6: header
            {
                throw new Exception("invalid header");
            }else System.out.println("header valid");
            //read header data:
            int format = data.getShort();
            int tracks = data.getShort();
            division = data.getShort();
            System.out.println("MIDI type: " + format);
            System.out.println("num. tracks: " + tracks);
            System.out.println("time division: " + division);
            int i = 0;
            while(i != tracks)
            {
                System.out.println("parsing track " + i);
                if(data.getInt() != 0x4D54726B)
                {
                    throw new Exception("Invalid track header");
                }
                int length = data.getInt();
                System.out.println("length of this track: " + length);
                byte trackContent[] = new byte[length];
                data.get(trackContent);
                //actual parsing:
                parseTrack(ByteBuffer.wrap(trackContent), i);
                i++;
            }
            System.out.println("parsing done!");
            
        }catch(Exception e)
        {
            System.out.println("error parsing song " + fileName + ":");
            e.printStackTrace();
        }
        return image;
    }
    public static void main(String[] args) throws Exception
    {
        //PrinterControl pc = new PrinterControl();
        //pc.init();
        File file = new File("input.mid");
        FileInputStream fin = new FileInputStream(file);
        byte fileContent[] = new byte[(int)file.length()];
        fin.read(fileContent);
        ByteBuffer data = ByteBuffer.wrap(fileContent);
        int headerA = data.getInt();
        int headerB = data.getInt();
        if(headerA != 0x4D546864 && headerB != 6)//"MThd", 6: header
        {
            throw new Exception("invalid header");
        }else System.out.println("header valid");
        //read header data:
        int format = data.getShort();
        int tracks = data.getShort();
        division = data.getShort();
        System.out.println("MIDI type: " + format);
        System.out.println("num. tracks: " + tracks);
        System.out.println("time division: " + division);
        int i = 0;
        while(i != tracks)
        {
            System.out.println("parsing track " + i);
            if(data.getInt() != 0x4D54726B)
            {
                throw new Exception("Invalid track header");
            }
            int length = data.getInt();
            System.out.println("length of this track: " + length);
            byte trackContent[] = new byte[length];
            data.get(trackContent);
            //actual parsing:
            parseTrack(ByteBuffer.wrap(trackContent), i);
            i++;
        }
        System.out.println("parsing done!");
        BufferedImage img = image.genImage();
        System.out.println("note data:");
        Collections.sort(image.validEvents);
        for(Event e:image.validEvents)
        {
            System.out.println(e);
        }
        System.out.println("saving...");
        File outputfile = new File("output.png");
        ImageIO.write(img, "png", outputfile);
        System.out.println("done!");
        
        System.out.println("---post processing");
        EventProcessor process = new EventProcessor(image.validEvents);

        
        process.process();
        System.out.println("---Post processing done. Outputting results");
        BlockSet image2 = new BlockSet(process.events);
        BufferedImage img2 = image2.genImage();
        System.out.println("saving...");
        File outputfile2 = new File("output2.png");
        ImageIO.write(img2, "png", outputfile2);
        System.out.println("done!");
        
        
        System.out.println("preparing to play song...");
        PlayerGenerator player = new PlayerGenerator(image2.validEvents, new DrumKit());
        Thread.sleep(10_000);//wait for calibration'n stuff
        player.assign(false);
        //TODO use output here!
    }
    
}
