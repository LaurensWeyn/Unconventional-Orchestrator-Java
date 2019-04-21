/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader.manipulation;

/**
 *
 * @author Laurens
 */
public class DrumMapping implements Comparable
{
    //note: drum is usually channel 10
    public final int noteVal;
    private final Drum name;
    public int drumID;
    public int qty;
    public DrumMapping(String line)
    {
        String subBits[] = line.split("-");
        noteVal = Integer.parseInt(subBits[0]);
        drumID = Integer.parseInt(subBits[1]);
        name = Drum.getWith(noteVal);
    }
    public DrumMapping(int noteVal)
    {
        this.noteVal = noteVal;
        name = Drum.getWith(noteVal);
        qty = 0;
        drumID = 0;
    }
    public DrumMapping(int noteVal, int drumID)
    {
        this.noteVal = noteVal;
        this.drumID = drumID;
        qty = 0;
        name = Drum.getWith(noteVal);
    }
    public DrumMapping(int noteVal, int drumID, int qty)
    {
        this.noteVal = noteVal;
        this.drumID = drumID;
        this.qty = qty;
        name = Drum.getWith(noteVal);
    }
    
    public void addQty()
    {
        qty++;
    }
    private String tag(String text,String tag)
    {
        return "<"+tag+">"+text+"</"+tag+">";
    }
    public boolean isImportant()
    {
        return qty != 0 && drumID != 0;
    }
    @Override
    public String toString()
    {
        String out = noteVal + " " + name + " [" + qty + " note" + (qty==1?"":"s") + "]--> #" + (drumID==0?"?":drumID);
        if(isImportant())out = tag(out, "b");
        return tag(out, "html");
    }
    public String toLine()
    {
        return noteVal + "-" + drumID;
    }

    @Override
    public int compareTo(Object o)
    {
        DrumMapping dm = (DrumMapping)o;
        return Integer.compare(qty, dm.qty);
    }
    public enum Drum
    {
        unknown(0),
        bassDrum2(35),
        bassDrum1(36),
        sideStick(37),
        snareDrum1(38),
        handClap(39),
        SnareDrum2(40),
        lowTom2(41),
        closedHighHat(42),
        lowTom1(43),
        pedalHighHat(44),
        midTom2(45),
        openHighHat(46),
        midTom1(47),
        highTom2(48),
        crashCymbal1(49),
        highTom1(50),
        rideCymbal1(51),
        chineseCymbal(52),
        rideBell(53),
        tamborine(54),
        splashCymbal(55),
        cowbell(56),
        crashCymbal2(57),
        vibraSlap(58),
        rideCymbal2(59),
        highBongo(60),
        lowBongo(61),
        muteHighConga(62),
        openHighConga(63),
        lowConga(64),
        highTimbale(65),
        lowTimbale(66),
        highAgogo(67),
        lowAgogo(68),
        cabasa(69),
        maracas(70),
        shortWhistle(71),
        longWhistle(72),
        shortGurio(73),
        longGurio(74),
        claves(75),
        highWoodBlock(76),
        lowWoodBlock(77),
        muteCuica(78),
        openCuica(79),
        muteTriangle(80),
        openTriangle(81);
        private final int ID;
        Drum(int ID)
        {
            this.ID = ID;
        }
        public boolean isVal(int noteVal)
        {
            return noteVal == ID;
        }
        public static Drum getWith(int ID)
        {
            for(Drum d:values())
            {
                if(d.isVal(ID))return d;
            }
            return unknown;
        }
    }
}
