/*
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.myport;

import java.io.IOException;

/**
 *
 * @author Laurens Weyn
 */
public class StdIOCOMPort extends StreamCOMPort
{
    
    public StdIOCOMPort() throws IOException
    {
        super(System.in, System.out);
    }
    
}
