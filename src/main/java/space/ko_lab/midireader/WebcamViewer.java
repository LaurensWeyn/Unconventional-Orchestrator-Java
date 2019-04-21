/* 
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package space.ko_lab.midireader;

//import org.bytedeco.javacpp.opencv_core.IplImage;
//import static org.bytedeco.javacpp.opencv_core.cvFlip;
//import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
//import org.bytedeco.javacv.CanvasFrame;
//import org.bytedeco.javacv.FrameGrabber;
//import org.bytedeco.javacv.VideoInputFrameGrabber;

/**
 *
 * @author Laurens
 */

public class WebcamViewer implements Runnable {
@Override
    public void run() {}    
    //final int INTERVAL=1000;///you may use interval
    /*IplImage image;
    CanvasFrame canvas = new CanvasFrame("Web Cam");
    public WebcamViewer() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    @Override
    public void run() {
        FrameGrabber grabber = new VideoInputFrameGrabber(0); 
        int i=0;
        try {
            grabber.start();
            IplImage img;
            while (true) {
                img = grabber.grab();
                if (img != null) {
                    //cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                    //cvSaveImage((i++)+"-capture.jpg", img);
                    // show image on window
                    canvas.showImage(img);
                }
                 //Thread.sleep(INTERVAL);
            }
        } catch (Exception e) {
        }
    }
    public static void main(String[] args)
    {
        new WebcamViewer().run();
    }*/
}
