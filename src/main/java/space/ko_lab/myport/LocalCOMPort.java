/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myport;

import jssc.*;



/**
 *
 * @author Laurens
 */
public class LocalCOMPort extends COMPort
{
    SerialPort port;
    public LocalCOMPort(String portName, int baud)throws SerialPortException
    {
        this(new SerialPort(portName), baud);
    }
    public LocalCOMPort(SerialPort port, int baud)throws SerialPortException
    {
        this.port = port;
        port.openPort();
        port.setParams(baud, 8, 1, 0);
    }
    public LocalCOMPort(SerialPort port)throws SerialPortException
    {
        this.port = port;
    }
    @Override
    public void writeString(String data)
    {
        try
        {
            port.writeString(data);
        } catch(SerialPortException e)
        {
            handleException(e);
        }
    }
    @Override
    public void writeInt(int data)
    {
        try
        {
            port.writeInt(data);
        } catch(SerialPortException e)
        {
            handleException(e);
        }
    }
    @Override
    public void writeBytes(byte[] data)
    {
        try
        {
            port.writeBytes(data);
        } catch(SerialPortException e)
        {
            handleException(e);
        }
    }
    @Override
    public void writeIntArray(int[] data)
    {
        try
        {
            port.writeIntArray(data);
        } catch(SerialPortException e)
        {
            handleException(e);
        }
    }
    @Override
    public void writeByte(byte data)
    {
        try
        {
            port.writeByte(data);
        } catch(SerialPortException e)
        {
            handleException(e);
        }
    }
    
    
    private void handleException(SerialPortException e)
    {
        
    }

    @Override
    public String readString()
    {
        try
        {
            return port.readString();
        }catch(SerialPortException e)
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
            return port.readBytes(1)[0];
        }catch(SerialPortException e)
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
            return port.readBytes();
        }catch(SerialPortException e)
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
            return port.readIntArray(1)[0];
        }catch(SerialPortException e)
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
            return port.readIntArray();
        }catch(SerialPortException e)
        {
            handleException(e);
            return null;
        }
    }
    
    @Override
    public int available()
    {
        try
        {
            return port.getInputBufferBytesCount();
        }catch(SerialPortException e)
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
            port.closePort();
        }catch(SerialPortException e)
        {
            handleException(e);
        }
    }

    @Override
    public byte[] readBytes(int len)
    {
        try
        {
            return port.readBytes(len);
        }catch(SerialPortException e)
        {
            handleException(e);
        }
        return null;
    }

    @Override
    public int[] readIntArray(int len)
    {
        try
        {
            return port.readIntArray(len);
        }catch(SerialPortException e)
        {
            handleException(e);
        }
        return null;
    }
    
}
