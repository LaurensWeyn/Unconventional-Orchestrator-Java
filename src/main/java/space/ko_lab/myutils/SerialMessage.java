/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myutils;

import java.util.Arrays;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author Laurens
 */
public class SerialMessage
{
    private final long delta;
    private final byte[] data;
    public SerialMessage(long delta, byte... data)
    {
        this.delta = delta;
        this.data = data;
    }
    public int packetSize()
    {
        return 8 + 4 + data.length;
    }
    public long getDelta()
    {
        return delta;
    }

    public byte[] getData()
    {
        return data;
    }
    public void SendData(SerialPort p) throws SerialPortException
    {
        p.writeBytes(data);
    }

    @Override
    public String toString()
    {
        return delta + ": " + Arrays.toString(data);
    }
    
}
