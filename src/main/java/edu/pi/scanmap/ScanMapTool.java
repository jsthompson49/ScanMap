package edu.pi.scanmap;

import main.java.edu.pi.scanmap.manager.QRCodeManager;
import main.java.edu.pi.scanmap.manager.WifiStrengthManager;
import main.java.edu.pi.scanmap.manager.WifiStrengthManager.WifiDetection;
import main.java.edu.pi.scanmap.util.FixedWindow;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ScanMapTool {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private final FixedWindow<WifiDetection> wifiDetectionFixedWindow = new FixedWindow<>(WifiDetection.class, 10);
    private QRCodeManager qrCodeManager = new QRCodeManager();
    private WifiStrengthManager wifiStrengthManager = new WifiStrengthManager(threadPool);
    private static String lastDetectedQRCode = "None";

    public static void main(final String[] args) {
        System.out.println("Starting ScanMap");

        final JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(800, 700);
        final JLabel label = new JLabel();
        final JLabel qrCode = new JLabel();
        frame.add(label);
        frame.add(qrCode);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final VideoCapture videoCapture = new VideoCapture(0);
        final Consumer<Image> imageConsumer = image -> {
            ImageIcon icon = new ImageIcon(image);
            label.setIcon(icon);
            qrCode.setText(lastDetectedQRCode);
        };
        final ScanMapTool scanMapTool = new ScanMapTool();

        scanMapTool.execute(videoCapture, imageConsumer);

        System.exit(0);
    }

    public void execute(final VideoCapture videoCapture, final Consumer<Image> imageConsumer) {
        threadPool.submit(new WifiStrengthDetectionTask());

        while (true) {
            final String qrCode = qrCodeManager.detectQRCode(videoCapture, imageConsumer);
            if (qrCode != null) {
                lastDetectedQRCode = qrCode;
                final WifiDetection lastWifiDetection = wifiDetectionFixedWindow.getItems().get(0);
                System.out.println(String.format("Detected qrCode:'%s' with last WIFI detection at %s",
                        qrCode, new Date(lastWifiDetection.getTimestamp())));
            }
        }
    }

    private class WifiStrengthDetectionTask implements Callable<Void> {
        @Override
        public Void call() throws Exception {
            while (true) {
                try {
                    final WifiDetection wifiDetection = wifiStrengthManager.detectAccessPoints();
                    if (wifiDetection != null) {
                        System.out.println("Adding WIFI detection at " + new Date(wifiDetection.getTimestamp()));
                        wifiDetectionFixedWindow.add(wifiDetection);
                    }

                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }
    }
}