/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myutils;

import java.nio.ByteBuffer;
import java.util.LinkedList;


/**
 *
 * @author Laurens
 */
public class SerialPack
{
    private final LinkedList<SerialMessage> instructions = new LinkedList<>();
    public SerialPack()
    {
        
    }
    public SerialPack(byte[] data)
    {
        ByteBuffer bb = ByteBuffer.wrap(data);
        int size = bb.getInt();
        while(bb.hasRemaining())
        {
            long delta = bb.getLong();
            int length = bb.getInt();
            byte[] msgData = new byte[length];
            int i = 0;
            while(i != length)
            {
                msgData[i] = bb.get();
                i++;
            }
            instructions.add(new SerialMessage(delta, msgData));
        }
        if(size != instructions.size())
        {
            throw new IllegalArgumentException("invalid input");
        }
    }
    public byte[] toData()
    {
        int size = 4;
        for(SerialMessage i:instructions)
        {
            size += i.packetSize();
        }
        ByteBuffer bb = ByteBuffer.allocate(size);
        bb.putInt(instructions.size());
        for(SerialMessage i:instructions)
        {
            byte[] iData = i.getData();
            bb.putLong(i.getDelta());
            bb.putInt(iData.length);
            for(byte b:iData)
            {
                bb.put(b);
            }
        }
        return bb.array();
    }
    public void add(SerialMessage sm)
    {
        instructions.add(sm);
    }
    public void add(long delta, byte... data)
    {
        instructions.add(new SerialMessage(delta, data));
    }
    
    public LinkedList<SerialMessage> getInstructions()
    {
        return instructions;
    }

    @Override
    public String toString()
    {
        String list = "";
        for(SerialMessage sm:instructions)
        {
            list += sm + "\n";
        }
        return list.trim();
    }
    
}
