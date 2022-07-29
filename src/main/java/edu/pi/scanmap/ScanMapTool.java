package edu.pi.scanmap;

import main.java.edu.pi.scanmap.manager.QRCodeManager;
import main.java.edu.pi.scanmap.manager.WifiStrengthManager;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.function.Consumer;

public class ScanMapTool {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private QRCodeManager qrCodeManager = new QRCodeManager();
    private WifiStrengthManager wifiStrengthManager = new WifiStrengthManager();

    public static void main(final String[] args) {
        System.out.println("Starting ScanMap");

        final JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(800, 700);
        final JLabel label = new JLabel();
        frame.add(label);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final VideoCapture videoCapture = new VideoCapture(0);
        final Consumer<Image> imageConsumer = image -> {
            ImageIcon icon = new ImageIcon(image);
            label.setIcon(icon);
        };
        final ScanMapTool scanMapTool = new ScanMapTool();

        scanMapTool.execute(videoCapture, imageConsumer);

        System.exit(0);
    }

    public void execute(final VideoCapture videoCapture, final Consumer<Image> imageConsumer) {
        while (true) {
            try {
                wifiStrengthManager.detectAccessPoints();
                //qrCodeManager.detectQRCodes(videoCapture, imageConsumer);

                //Thread.sleep(2000);
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }
}