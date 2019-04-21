/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myutils;


import java.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author Laurens
 */
public class Message
{
    private MType type;
    private byte data[];
    private String dataOrig;
    public static PrintStream log = System.out;
    private String source;
    
    public enum MType
    {
        ack,
        command,
        argument,
        rawData,
        intData,
        doubleData,
        file,
        SerialMessage,
    }
    public enum Command
    {
        printTest,
        printThermalImg,
        powerMode,
        fileUpload,
        fileDownload,
        playSong,
        initSong,
        stopSong,
    }
    
    
    private void logSend(String data)
    {
        source = data;
        if(log == null)return;
        log.println(new Timestamp(new Date().getTime()) + ": sent " + data);
    }
    private void logRecieve(String data)
    {
        source = data;
        if(log == null)return;
        log.println(new Timestamp(new Date().getTime()) + ": recieved " + data);
    }
    private int checksum(byte[] data)
    {
        int checkData = 0;
        for(byte b:data)
        {
            checkData ^=b;
        }
        return checkData;
    }
    private void ensureType(MType type)throws IllegalStateException
    {
        if(this.type != type)throw new IllegalStateException("not " + type + " message, type is " + this.type);
    }
    private String dataAsString()
    {
        return new String(data);
    }
    ////////////////////
    //message decoding//
    ////////////////////
    public boolean isAck()
    {
        return type == MType.ack && Arrays.equals(data, new byte[]{(byte)1});
    }
    public Command getCommand()
    {
        ensureType(MType.command);
        return Command.valueOf(dataAsString());
    }
    public String getArgument()
    {
        ensureType(MType.argument);
        return dataAsString();
    }
    public byte[] getFileRaw()
    {
        ensureType(MType.file);
        return data;
    }
    public int[] getIntData()
    {
        ensureType(MType.intData);
        int intData[] = new int[data.length / 4];
        ByteBuffer bb = ByteBuffer.wrap(data);
        int i = 0;
        while(i != intData.length)
        {
            intData[i] = bb.getInt();
            i++;
        }
        return intData;
    }
    public SerialPack getSerialMessage()
    {
        ensureType(MType.SerialMessage);
        return new SerialPack(data);
    }
    public void saveFile(File file)throws IOException
    {
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            fos.write(data);
        }
    }
    ////////////////////
    //message encoding//
    ////////////////////
    public Message(boolean ack)
    {
        type = MType.ack;
        if(ack) data = new byte[]{(byte)1};
        else data = new byte[]{(byte)0};
    }
    public Message(File file)throws IOException
    {
        data = Files.readAllBytes(file.toPath());
        type = MType.file;
    }
    public Message(Command command)
    {
        type = MType.command;
        data = command.toString().getBytes();
    }
    public Message(String argument)
    {
        type = MType.argument;
        data = argument.getBytes();
    }
    public Message(SerialPack messages)
    {
        type = MType.SerialMessage;
        data = messages.toData();
    }
    public Message(int[] data)
    {
        type = MType.intData;
        ByteBuffer bb = ByteBuffer.wrap(new byte[data.length * 4]);
        for(int i:data)
        {
            bb.putInt(i);
        }
        this.data = bb.array();
    }
    //message IO:
    public Message(NetworkReader nr) throws IOException
    {
        //logging:
        boolean backed = nr.isBacked();
        String input = nr.readLine();
        if(backed == false)logRecieve(input);
        
        //interpreting:
        try
        {
            String bits[] = input.split(",");//format: type,size,check,data\n
            if(bits.length != 4)throw new IOException("incorrect number of seperators");
        
            type = MType.valueOf(bits[0]);
            int size = Integer.parseInt(bits[1]);
            int check = Integer.parseInt(bits[2]);
            data = Base64.getDecoder().decode(bits[3]);
            if(data.length != size)throw new IOException("incorrect ammount of data");
            if(check != checksum(data))throw new IOException("checksum does not match");
        }catch(IOException | NumberFormatException e)
        {
            throw new IOException("Incorrect packet format: " + e);
        }
    }
    public void send(Socket s) throws IOException
    {
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        //generate format: type,size,check,data\n
        String output = type + "," + data.length + "," + checksum(data) + "," + Base64.getEncoder().encodeToString(data);
        out.println(output);
        logSend(output);
    }
    public static void testEncode()
    {
        byte[] data = {(byte)123,(byte)456,(byte)789,(byte)456};
        System.out.println("Encode data: " + Base64.getEncoder().encodeToString(data));
    }
}
