/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myport;

/**
 *
 * @author Laurens
 */
public abstract class COMPort
{
    public abstract void writeString(String data);
    public abstract void writeByte(byte data);
    public abstract void writeBytes(byte... data);
    public abstract void writeInt(int data);
    public abstract void writeIntArray(int... data);
    
    
    public abstract String readString();
    public abstract byte readByte();
    public abstract byte[] readBytes();
    public abstract byte[] readBytes(int len);
    public abstract int readInt();
    public abstract int[] readIntArray();
    public abstract int[] readIntArray(int len);
    
    public abstract int available();
    public abstract void close();
}
