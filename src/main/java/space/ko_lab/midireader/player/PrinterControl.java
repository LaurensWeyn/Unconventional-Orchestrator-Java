/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

import space.ko_lab.myutils.SerialMessage;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import jssc.SerialPortException;
import space.ko_lab.midireader.core.Note;

/**
 *
 * @author Laurens
 */
public class PrinterControl extends Instrument
{
    //final static double XYSteps = 80;  //steps per mm
    //final static double ZSteps  = 2020;//steps per mm
    //final static int maxX = 90, maxY = 100, maxZ = 100;
    
    final static double XYSteps = 80.50;  //steps per mm
    final static double ZSteps  = 405.6;//steps per mm
    final static int maxX = 180, maxY = 180, maxZ = 180;
    
    
    public boolean playingX, playingY, playingZ;
    int currentLine = 1;
    public static boolean assumeLine1 = false;
    public PrinterControl(String configLine)
    {
        super(configLine);
        //TODO take in steps per mm
    }
    public void sendCommand(long delta, String cmd)
    {
        messages.add(new SerialMessage(delta, cmd.getBytes()));
    }
    public void sendMCode(int delta, int ID, String... data)
    {
        String cmd = "M" + ID;
        for(String s:data)
        {
            cmd += " " + s;
        }
        sendCommand(delta, cmd);
    }
    public void sendCode(long delta, int ID, String... data)
    {
        String cmd = "G" + ID;
        for(String s:data)
        {
            cmd += " " + s;
        }
        sendCommand(delta, cmd);
    }

    double cx=0, cy=0, cz=0;
    private double distance(double x, double y, double z)
    {
        return Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cy - y, 2) + Math.pow(cz - z, 2));
    }
    public void moveTo(long delta, double x, double y,double z, double time)
    {
        if(PlayerGenerator.playNotes == false)return;
        double f = (distance(x, y, z) / (time / 60));
        if(f == Double.NaN)f = 4800.0;//in case time is 0, use default speed (we won't be going anywhere propably though)
        sendCode(delta, 0, "X" + x, "Y" + y, "Z" + z, "F" + f);// speed = mm/min
        cx = x; cy = y; cz = z;
    }
    public void moveTo(long delta, double x, double y, double z)
    {
        if(PlayerGenerator.playNotes == false)return;
        sendCode(delta, 0, "X" + x, "Y" + y, "Z" + z, "F4800");
        cx = x; cy = y; cz = z;
    }
    public void moveTo(long delta, int x, int y)
    {
        if(PlayerGenerator.playNotes == false)return;
        sendCode(delta, 0, "X" + x, "Y" + y, "F4800");
        cx = x; cy = y;
    }
    public void homeXY(long delta)
    {
        sendCode(delta, 28, "X0", "Y0");//checksum 1?
    }
    public void homeZ(int delta)
    {
        sendCode(delta, 28, "Z0");
    }
    
    double xDir = 1, yDir = 1, zDir = 1;
    private double noteX(Note note, double time)throws Exception
    {
        playingX = note != null;
        if(PlayerGenerator.playNotes == false || note == null) return cx;
        double movement = (note.getFreq() * time) /XYSteps;
        double pos = cx + movement * xDir;
        if(pos > maxX || pos < 0)
        {
            xDir *= -1;
            pos = cx + movement * xDir;
            if(pos > maxX || pos < 0)
            {
                throw new Exception("X cannot move for that long!");
            }
        }
        return pos;
    }
    private double noteY(Note note, double time)throws Exception
    {
        playingY = note != null;
        if(PlayerGenerator.playNotes == false || note == null) return cy;
        double movement = (note.getFreq() * time) /XYSteps;
        double pos = cy + movement * yDir;
        if(pos > maxY || pos < 0)
        {
            yDir *= -1;
            pos = cy + movement * yDir;
            if(pos > maxY || pos < 0)
            {
                throw new Exception("Y cannot move for that long!");
            }
        }
        return pos;
    }
    private double noteZ(Note note, double time)throws Exception
    {
        playingZ = note != null;
        if(PlayerGenerator.playNotes == false || note == null) return cz;
        double movement = (note.getFreq() * time) /ZSteps;
        double pos = cz + movement * zDir;
        if(pos > maxZ || pos < 0)
        {
            zDir *= -1;
            pos = cz + movement * zDir;
            if(pos > maxZ || pos < 0)
            {
                throw new Exception("Z cannot move for that long!");
            }
        }
        return pos;
    }
    /*public void playSet(long delta, ArrayList<Note> current, double time)
    {
            playingX = false;
            playingY = false;
            playingZ = false;
            int size = current.size();
            //TODO put printerplayer class in charge of all this
            switch(size)
            {
                case 0://do nothing
                    moveTo(delta, cx, cy, cz);
                    break;
                case 1:
                    playNotes(delta, null, null, current.get(0), time);
                    break;
                case 2:
                    playNotes(delta, null, current.get(0), current.get(1), time);
                    break;
                case 3:
                    playNotes(delta, current.get(1), current.get(0), current.get(2), time);
                    break;
                default:
                    System.out.println("too many notes!(" + current.size() + "), sorting");
                    //currentNotes.sort(null);//sort the notes! (by pitch)
                    //play most recent notes:
                    playNotes(delta, current.get(size - 1), current.get(size - 2), current.get(size - 3), time);
                    //pc.playNotes(currentNotes.get(0), currentNotes.get(1), currentNotes.get(2), delta);
                    break;
            }
    }*/
    public void playNotes(long delta, Note noteA, Note noteB, Note noteC, double time)
    {
        if(time == 0.0 || time == Double.NaN)return;
        try
        {
            moveTo(delta, noteX(noteC, time), noteY(noteB, time), noteZ(noteA, time), time);
        }catch(Exception e)
        {
            System.out.println("error playing note: "+ e);
        }
    }

    @Override
    public void playSet(long delta, Note[] notes, double time)
    {
        playingX = false;
        playingY = false;
        playingZ = false;
        int size = notes.length;
        switch(size)
        {
            case 0://do nothing
                //moveTo(delta, cx, cy, cz);
                break;
            case 1:
                playNotes(delta, null, null, notes[0], time);
                break;
            case 2:
                playNotes(delta, null, notes[0], notes[1], time);
                break;
            case 3:
                playNotes(delta, notes[1], notes[0], notes[2], time);
                break;
            default:
                System.out.println("too many notes!(" + size + "), sorting");
                //play most recent notes:
                playNotes(delta, notes[size - 1], notes[size - 2], notes[size - 3], time);
                break;
        }
    }

    @Override
    public void initSerial(int baud)throws SerialPortException
    {
        super.initSerial(115200);
        try
        {
            Thread.sleep(5000);//wait for some COM init data
        } catch(InterruptedException ex)
        {
        }
        System.out.println("Printer boot wait done");
        port.readString();//clear init data
        
        if(assumeLine1)//guaranteed reset; start at line 1
        {
            //TODO find out how to actually reset the printer with COM control lines
            currentLine = 1;
            return;
        }
        System.out.println("Clearing line");
        port.writeString("M110 N1\n");//set line counter to 1
        currentLine = 1;//in case it actually worked
        String in = "";
        System.out.println("Waiting for OK");
        while(in.contains("ok") == false)
        {
            if(port.getInputBufferBytesCount() > 0)in += port.readString();
        }
        System.out.println("Recieved OK");
        String bits[] = in.split("\n");
        if(bits[1].contains("Resend: "))
        {
            currentLine = Integer.parseInt(bits[1].split(" ")[1]);            
            //throw new Exce("unexpected return data");
        }else if(bits[1].contains("checksum mismatch"))
        {
            currentLine = Integer.parseInt(bits[1].split(" ")[4]);            
        }else System.out.println("unexpected return data, assuming line 1");
        System.out.println(Arrays.toString(bits));
        //if(bits[1].equals("Checksum"))currentLine = Integer.parseInt(bits[1].split(" ")[])
        
        //System.out.println("found printer line as " + currentLine);
        startup();
    }

    @Override
    public void clear()
    {
        super.clear();//clear the buffers
        cx = 0; cy = 0; cz = 0;//we'll be at 0,0,0
    }
    
    @Override
    public void startup() throws SerialPortException
    {
        //TODO replace this with standard Gcode init from file?
        playingX = false;
        playingY = false;
        playingZ = false;
        PrinterControl pc = this;
        pc.clear();
        pc.sendCode(-1, 90);//absolute
        if( currentLine < 10)//assume printer isn't calibrated
        {
            pc.homeXY(-1);
            pc.moveTo(-1, 0, 0);//homeY = 100, our home = 0
            pc.homeZ(-1);//safely sets home Z higher than usual ("highest" at Y0)
        }
        //assume it is now calibrated
        pc.moveTo(-1, 0, 0, 0);
        //send generated data:
        for(SerialMessage m:pc.messages.getInstructions())
        {
            sendCmd(m);
        }
        pc.clear();
    }

    @Override
    public void sendCmd(byte... data) throws SerialPortException
    {
        if(transmit)
        {
            System.out.println("WARN: printer note already being sent, waiting...");
        }
        while(transmit)
        {
            try{Thread.sleep(1);}catch(Exception e){}
        }
        transmit = true;//start
        String cmd = "";
        try
        {
            cmd = "N" + currentLine + " " + new String(data, "US-ASCII");
        } catch(UnsupportedEncodingException ex){ex.printStackTrace();}
        //System.out.println("sending " + cmd);
        int checksum = 0;
        int i = 0;
        while(i != cmd.length())
        {
            char c = cmd.charAt(i);
            /*if(c == '.')
            {
                System.out.println("DOT");
                checksum = checksum ^ 46;//TODO remove all this debug stuff
            }else*/ checksum = checksum ^ cmd.charAt(i);
            i++;
        }
        checksum &= 0xff;  // Defensive programming...
        cmd += "*" + checksum;
        
        //System.out.println("sending " + cmd);
        port.readBytes();//first empty input buffer
        port.writeString(cmd + "\n");//write command
        String result = "";
        while(!result.contains("\n"))//wait for ACK/NACK
        {
            String read = port.readString();
            if(read != null)result += read;
        }
        //interpret ACK/NACK
        if(result.startsWith("ok"))
        {
            currentLine++;
        }
        else
        {
            System.out.println("WARN: printer resend required. Result: " + result);
            int newLine = -1;
            for(String s:result.split(" "))
            {
                System.out.println("searching for int in " + s);
                try
                {
                    newLine = Integer.parseInt(s);
                }catch(Exception e){}
            }
            if(newLine == -1)
            {
                System.out.println("ERR: No new line number found.");
                newLine = currentLine + 1;
                System.out.println("assuming next line " + newLine);
                currentLine = newLine;
                transmit = false;
                return;//skip this command, perhaps it did run?
            }
            else currentLine = newLine;
            transmit = false;//so next line can run 
            sendCmd(data);//resend command (yay recursion!)
            //no point setting transmit to true again, we'll set it to false immediately after
        }
        
        transmit = false;//done
    }

}
