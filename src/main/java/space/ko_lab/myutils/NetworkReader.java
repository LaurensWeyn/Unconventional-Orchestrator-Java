/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Laurens
 */
public class NetworkReader
{
    private String lastLine = "";
    private boolean backed = false;
    private BufferedReader br;
    public NetworkReader(Socket socket) throws IOException
    {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    public String readLine() throws IOException
    {
        //one reversed, send this line instead
        if(backed)
        {
            backed = false;
            return lastLine;
        }
        else
        {
            lastLine = br.readLine();
            return lastLine;
        }
    }
    public String peekLine() throws IOException
    {
        String data = readLine();
        backed = true;
        return data;
    }
    public Message readMsg() throws IOException
    {
        return new Message(this);
    }
    public Message peekMsg() throws IOException
    {
        return new Message(this);
    }
    public boolean isBacked()
    {
        return backed;
    }
    public String lastLine()
    {
        return lastLine;
    }
    public void unRead() throws IOException
    {
        if(backed == true)throw new IOException("cannot reverse further");
        backed = true;
    }
}
