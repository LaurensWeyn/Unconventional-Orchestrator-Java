/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader;

import space.ko_lab.midireader.player.PlayerGenerator;
import space.ko_lab.midireader.core.BlockSet;
import space.ko_lab.midireader.player.PrinterControl;
import space.ko_lab.midireader.player.FloppyControl;
import eu.hansolo.enzo.lcd.Lcd;
import eu.hansolo.enzo.lcd.LcdBuilder;
import eu.hansolo.enzo.led.Led;
import eu.hansolo.enzo.led.LedBuilder;
import eu.hansolo.enzo.sixteensegment.SixteenSegment;
import eu.hansolo.enzo.sixteensegment.SixteenSegmentBuilder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.scene.image.Image;
import java.util.Arrays;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

import space.ko_lab.midireader.player.Instrument;

/**
 *
 * @author Laurens
 */
public class PlayGUI extends Application implements Runnable
{

    //song related:

    public static PlayerGenerator player;
    public static long tpp = 25_000_000l;//time per pixel (nanoseconds)
    public static double pps;//time per pixel (nanoseconds)
    public static String text = "Song name here\nartist name here";
    //internal track vars:
    private long lastTimerUpdate;
    private long lastTimerText;
    private long lastTimerDisplay;
    private long lastTimerPlayAnim;
    private long startTime;
    private int textPos = 0;

    private boolean playToggle = false;

    //entities:
    private Lcd statusDisplay;
    private AnimationTimer timer;
    private Led[] printrLED;
    private Led[] floppyLED;
    private Led[] percusLED;
    private final static int numLeds = 4;

    //other:
    private int portPos = 0;
    private int imgWidth = 0;
    private static int imgLength;
    //private int displayState = 1;
    //constants:
    private final static int windowHeight = 720;
    private final static int barSpacing = 10;
    private final static int barHeight = 110;

    //calc. constants:
    private final static int realBarHeight = barHeight + barSpacing * 2;
    private final static int imgHeight = windowHeight - realBarHeight;
    private final static int ledSize = (realBarHeight - 10 * 2) / numLeds;
    private final ImageView iv = new ImageView();
    private SixteenSegment seg[] = new SixteenSegment[8];

    static FloppyControl floppy;
    static PrinterControl printer;

    public static void play(PlayerGenerator player) throws IOException
    {
        //TODO update this for new play system
        PlayGUI.player = player;
        //init image
        BlockSet bg = new BlockSet(player.events);
        BufferedImage bi = bg.genImage();
        PlayGUI.tpp = (long) (bg.songLen * 1000000000.0) / ((long) bi.getHeight() + imgHeight);
        PlayGUI.pps = (double) (bi.getHeight()) / ((double) bg.songLen * 1000000000.0);
        imgLength = bi.getHeight();
        System.out.println("imgheight: " + bi.getHeight());
        File outputfile = new File("render.png");
        ImageIO.write(bi, "png", outputfile);
        //TODO don't hardcode this instrument types?
        floppy = (FloppyControl) Instrument.list.get(0);
        printer = (PrinterControl) Instrument.list.get(1);
        text = player.text;
        launch((String) null);
    }

    public Led buildLed(Color col)
    {
        return LedBuilder.create()
                .ledType(eu.hansolo.enzo.led.Led.LedType.SQUARE)
                .prefSize(ledSize, ledSize)
                .ledColor(col)
                .build();
    }

    public void startup()
    {
        String spacer = "";
        int j = 0;
        while(j != seg.length)
        {
            j++;
            spacer += " ";
        }
        text = text.replaceAll("\n", spacer);
        text = spacer + text + spacer;

        try
        {
            //init segments
            for(int i = 0; i < seg.length; i++)
            {
                seg[i] = SixteenSegmentBuilder.create()
                        .prefHeight(barHeight)
                        .prefWidth(90)
                        .character(" ")
                        .segmentStyle(SixteenSegment.SegmentStyle.YELLOW)
                        .build();

            }
            seg[0].setCharacter("R");
            seg[1].setCharacter("E");
            seg[2].setCharacter("A");
            seg[3].setCharacter("D");
            seg[4].setCharacter("Y");
            //init leds
            percusLED = new Led[2];
            printrLED = new Led[printer.getChannels()];
            floppyLED = new Led[floppy.getChannels()];
            int i = 0;
            while(i != percusLED.length)
            {
                percusLED[i] = buildLed(Color.WHITE);
                i++;
            }
            i = 0;
            while(i != floppyLED.length)
            {
                floppyLED[i] = buildLed(Color.RED);
                i++;
            }
            i = 0;
            while(i != printrLED.length)
            {
                printrLED[i] = buildLed(Color.GREEN);
                i++;
            }

            //init display
            statusDisplay = LcdBuilder.create()
                    .valueFont(Lcd.LcdFont.LCD)
                    .prefWidth(400)
                    .prefHeight(barHeight)
                    .styleClass(Lcd.STYLE_CLASS_DARKBLUE)
                    .foregroundShadowVisible(true)
                    .crystalOverlayVisible(false)
                    .trend(Lcd.Trend.UNKNOWN)
                    .trendVisible(true)
                    .titleVisible(true)
                    .maxValue(Double.MAX_VALUE)
                    //.lowerCenterText("lower")
                    //.lowerCenterTextVisible(true)
                    .title("Notes played")
                    .titleFont(STYLESHEET_CASPIAN)
                    .build();
            statusDisplay.setMainInnerShadowVisible(true);
            statusDisplay.setForegroundShadowVisible(true);

            Image image = new Image(new FileInputStream("render.png"));
            iv.setImage(image);
            portPos = (int) image.getHeight() - imgHeight;
            iv.setViewport(new Rectangle2D(portPos, 0, imgWidth, imgHeight));
            imgWidth = (int) image.getWidth();
        } catch(Exception e)
        {
            System.out.println("error during FX component init:");
            e.printStackTrace();
        }
    }
    int imgTick = 0;
    boolean waitState = false;
    boolean started = false;

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException
    {
        startup();
        System.out.println("PRIMARY STAGE: " + primaryStage);
        statusDisplay.setOnMousePressed(new EventHandler<MouseEvent>()
        {

            @Override
            public void handle(MouseEvent event)
            {
                if(started)
                {
                    return;//don't do this twice!
                }
                startTime = System.nanoTime();
                started = true;
                //timer.start();
                new Thread(player).start();
                //player.start();
                System.out.println("event started!");
            }
        });
        iv.setViewport(new Rectangle2D(0, portPos, imgWidth, imgHeight));
        timer = new AnimationTimer()
        {
            @Override
            public void handle(long now)
            {
                if(started)
                {
                    if(imgTick == 0)
                    {
                        portPos = imgLength - imgHeight - (int) (pps * (double) (System.nanoTime() - startTime));
                        iv.setViewport(new Rectangle2D(0, portPos, imgWidth, imgHeight));
                    } else
                    {
                        imgTick = 3;//update image every X ticks
                    }                    //portPos-=1;
                    //lastTimerUpdate = now;
                    statusDisplay.setValue(player.notesPlayed);

                    //set LED status based on new player channel systems
                    int j = 0;
                    while(j != percusLED.length)
                    {
                        percusLED[j].setOn(floppy.drumPlaying(j));
                        j++;
                    }
                    j = 0;
                    while(j != floppyLED.length)
                    {
                        floppyLED[j].setOn(floppy.getPlayingNotes()[j] != null);
                        j++;
                    }
                    j = 0;
                    while(j != printrLED.length)
                    {
                        printrLED[j].setOn(printer.getPlayingNotes()[j] != null);
                        j++;
                    }
                    
                    
                    if(now > lastTimerPlayAnim + 500_000_000l)
                    {
                        statusDisplay.setTrend(playToggle ? Lcd.Trend.STEADY : Lcd.Trend.UNKNOWN);
                        playToggle = !playToggle;
                        lastTimerPlayAnim = now;

                    }
                    if(now > lastTimerText + 250_000_000l)
                    {
                        String subStr = text.substring(textPos, textPos + seg.length);
                        //set characters:
                        for(int i = 0; i < seg.length; i++)
                        {
                            seg[i].setCharacter(subStr.charAt(i) + "");
                        }
                        textPos++;
                        //overflow:
                        if(textPos + seg.length > text.length())
                        {
                            textPos = 0;
                        }
                        lastTimerText = now;
                    }
                } else//do SOMETHING to stop recording software from freaking out
                {
                    printrLED[0].setOn(waitState);
                    percusLED[0].setOn(waitState);
                    waitState = !waitState;
                }
            }
        };
        timer.start();
        try
        {
            VBox root = new VBox();
            HBox base = new HBox();
            GridPane leds = new GridPane();

            //leds:
            //row 1: top floppies
            leds.add(floppyLED[7], 2, 0);
            leds.add(floppyLED[6], 3, 0);
            leds.add(floppyLED[5], 4, 0);
            //row 2: bottom floppies
            leds.add(floppyLED[4], 0, 1);
            leds.add(floppyLED[3], 1, 1);
            leds.add(floppyLED[2], 2, 1);
            leds.add(floppyLED[1], 3, 1);
            leds.add(floppyLED[0], 4, 1);
            //row 3: printer and drums
            leds.add(printrLED[2], 0, 2);
            leds.add(printrLED[0], 1, 2);
            leds.add(printrLED[1], 2, 2);
            
            leds.add(percusLED[1], 3, 2);
            leds.add(percusLED[0], 4, 2);
            /*int i = 0;
            for(Led l : floppyLED)
            {
                leds.add(l, i, 0);
                i++;
            }
            i = 0;
            for(Led l : printrLED)
            {
                leds.add(l, i, 1);
                i++;
            }
            i = floppyLED.length;
            for(Led l : percusLED)
            {
                i--;
                leds.add(l, i, 1);
            }*/
            //leds.setFillWidth(true);
            leds.setAlignment(Pos.CENTER);
            //base:
            base.setStyle("-fx-background-color: #000000;");
            base.setPadding(new Insets(barSpacing));

            base.getChildren().add(statusDisplay);
            base.getChildren().add(leds);
            
            base.getChildren().addAll(Arrays.asList(seg));//text display
            HBox.setHgrow(leds, Priority.ALWAYS);
            //base.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().add(iv);//notes
            root.getChildren().add(base);//status display
            root.setStyle("-fx-background-color: #FFFFFF;");
            Scene scene = new Scene(root, 1280, windowHeight);

            primaryStage.setTitle("stepper MIDI GUI");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            play(player);
        } catch(IOException ex)
        {
            System.out.println("error playing: " + ex);
            ex.printStackTrace();
        }
    }

}
