/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader;

import java.io.File;
import space.ko_lab.midireader.core.Note;
import space.ko_lab.midireader.manipulation.Mapping;
import space.ko_lab.midireader.player.Instrument;

/**
 *
 * @author Laurens
 */
public class SystemTest
{
    public static void main(String[] args)throws Exception
    {
         Instrument.loadInstruments(new File("instruments.csv"));
         Mapping.loadMapList(new File("mappings.csv"));
         
         System.out.println("configs loaded sucessfully!");
         Instrument floppy = Instrument.findInstrument("F8");
         System.out.println(floppy.getPortName());
         floppy.initSerial(115200);
         floppy.startup();
         floppy.setNote("F1", new Note(30));
         floppy.commit(2000000000L, 1);
         floppy.setNote("F2", new Note(40));
         floppy.commit(3000000000L, 1);
         floppy.setNote("F3", new Note(50));
         floppy.commit(4000000000L, 1);
         floppy.setNote("F4", new Note(60));
         floppy.commit(5000000000L, 1);
         floppy.setNote("F5", new Note(68));
         floppy.commit(6000000000L, 1);
         floppy.setNote("F6", new Note(80));
         floppy.commit(7000000000L, 1);
         floppy.setNote("F7", new Note(90));
         floppy.commit(8000000000L, 1);
         floppy.setNote("F8", new Note(100));
         floppy.commit(9000000000L, 1);
         floppy.setNote("F8", new Note(52));
         floppy.setNote("F7", new Note(52));
         floppy.setNote("F6", new Note(52));
         floppy.setNote("F5", new Note(52));
         floppy.setNote("F4", new Note(52));
         floppy.setNote("F3", new Note(52));
         floppy.setNote("F2", new Note(52));
         floppy.setNote("F1", new Note(52));
         floppy.commit(10000000000L, 1);//10 seconds
         floppy.commit(11000000000L, 1);
         System.out.println(floppy.getData());
         System.out.println("start test");
         floppy.play(System.nanoTime());
         System.out.println("done");
    }
}
