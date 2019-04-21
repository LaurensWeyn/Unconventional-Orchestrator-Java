/*
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Laurens Weyn
 */
public class StreamCOMPort extends COMPort
{
    InputStream in;
    OutputStream out;
    public StreamCOMPort(InputStream in, OutputStream out)throws IOException
    {
        this.in = in;
        this.out = out;
    }
    
    @Override
    public void writeString(String data)
    {
        try
        {
            out.write(data.getBytes());
        } catch(IOException e)
        {
            handleException(e);
        }
    }

    @Override
    public void writeByte(byte data)
    {
        try
        {
            out.write(data);
        } catch(IOException e)
        {
            handleException(e);
        }
    }

    @Override
    public void writeBytes(byte[] data)
    {
        try
        {
            out.write(data);
        } catch(IOException e)
        {
            handleException(e);
        }
    }

    @Override
    public void writeInt(int data)
    {
        try
        {
            out.write(data);
        } catch(IOException e)
        {
            handleException(e);
        }
    }

    @Override
    public void writeIntArray(int[] data)
    {
        try
        {
            for(int i:data)out.write(i);
        } catch(IOException e)
        {
            handleException(e);
        }
    }

    @Override
    public String readString()
    {
        try
        {
            String data = "";
            while(in.available() > 0)data = data + ((char)in.read());
            return data;
        }catch(IOException e)
        {
            handleException(e);
            return null;
        }
    }

    @Override
    public byte readByte()
    {
        try
        {
            return (byte)in.read();
        } catch(IOException e)
        {
            handleException(e);
            return 0;
        }
    }

    @Override
    public byte[] readBytes()
    {
        try
        {
            byte buffer[] = new byte[in.available()];
            in.read(buffer);
            return buffer;
        } catch(IOException e)
        {
            handleException(e);
            return null;
        }
    }

    @Override
    public int readInt()
    {
        try
        {
            return in.read();
        } catch(IOException e)
        {
            handleException(e);
            return 0;
        }
    }

    @Override
    public int[] readIntArray()
    {
        try
        {
            byte buffer[] = new byte[in.available()];
            in.read(buffer);
            int intBuffer[] = new int[buffer.length];
            int i = 0;
            while(i != intBuffer.length)
            {
                intBuffer[i] = (int)buffer[i];
                i++;
            }
            return intBuffer;
        } catch(IOException e)
        {
            handleException(e);
            return null;
        }
    }
    
    private void handleException(IOException e)
    {
        
    }

    @Override
    public int available()
    {
        try
        {
            return in.available();
        }catch(IOException e)
        {
            handleException(e);
            return 0;
        }
    }

    @Override
    public void close()
    {
        try
        {
            in.close();
            out.close();
        }catch(IOException e)
        {
            handleException(e);
        }
    }

    @Override
    public byte[] readBytes(int len)
    {
        try
        {
            byte data[] = new byte[len];
            in.read(data, 0, len);
            return data;
        }catch(IOException e)
        {
            handleException(e);
            return null;
        }
    }

    @Override
    public int[] readIntArray(int len)
    {
        try
        {
            byte data[] = new byte[len];
            in.read(data, 0, len);
            int intBuffer[] = new int[data.length];
            int i = 0;
            while(i != intBuffer.length)
            {
                intBuffer[i] = (int)data[i];
                i++;
            }
            return intBuffer;
        }catch(IOException e)
        {
            handleException(e);
            return null;
        }
    }
    
    
}
