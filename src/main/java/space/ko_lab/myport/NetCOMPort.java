/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myport;

import java.io.IOException;
import java.net.Socket;

import space.ko_lab.myutils.MyTimer;

/**
 *
 * @author Laurens
 */
public class NetCOMPort extends StreamCOMPort
{
    public NetCOMPort(Socket socket, String serviceName)throws IOException
    {
        this(socket);
        MyTimer.waitMs(100);
        //out.write(0);//null terminate
        System.out.println("connecting to service " + serviceName);
        out.write(serviceName.getBytes());
        out.write(0);//null terminate
        int read = in.read();
        //if(read != 6)throw new IOException("Port not opened, server fault; code " + read);
    }
    public NetCOMPort(Socket socket)throws IOException
    {
        super(socket.getInputStream(),socket.getOutputStream());
    }
    
}
