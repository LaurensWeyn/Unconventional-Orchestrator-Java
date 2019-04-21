/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.player;

import java.io.IOException;
import java.net.Socket;
import space.ko_lab.myutils.Message;
import space.ko_lab.myutils.NetworkReader;

/**
 *
 * @author Laurens
 */
public class NetworkPlayer extends Player
{
    private boolean playing = false;
    Socket socket;
    NetworkReader nr;
    public NetworkPlayer() throws IOException
    {
        this("192.168.1.92");//default server IP
    }
    public NetworkPlayer(String adress) throws IOException
    {
        socket = new Socket(adress, 9898);
        nr = new NetworkReader(socket);
    }
    @Override
    public void init() throws Exception
    {
        //do nothing
    }

    @Override
    public void play()
    {
        playing = true;
        try
        {
            System.out.println("starting server playback");
            new Message(Message.Command.playSong).send(socket);
            for(Instrument i:Instrument.list)
            {
                new Message(i.getData()).send(socket);
            }
            if(nr.readMsg().isAck())
            {
                playing = true;
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if(nr.readMsg().isAck() == false)
                            {
                                throw new IOException("Playback failed (server side)");
                            }
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                            playing = false;
                        }
                    }
              }.start();
            }else throw new IOException("starting playback failed (server side)");
        }catch(IOException e)
        {
            e.printStackTrace();
            playing = false;
        }
    }

    @Override
    public void stop()
    {
        try
        {
            if(playing)
            {   
                playing = false;
                new Message(Message.Command.stopSong).send(socket);
                if(nr.readMsg().isAck() == false)throw new IOException("stop playback failed (server side)");
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPlaying()
    {
        return playing;
    }

    @Override
    public void reset() throws Exception
    {
        new Message(Message.Command.initSong).send(socket);
        if(nr.readMsg().isAck() == false)throw new Exception("init failed (server side)");
    }
    
}
