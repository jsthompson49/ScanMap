package edu.pi.scanmap;

import main.java.edu.pi.scanmap.manager.MessageManager;
import main.java.edu.pi.scanmap.manager.QRCodeManager;
import main.java.edu.pi.scanmap.manager.WifiStrengthManager;
import main.java.edu.pi.scanmap.manager.WifiStrengthManager.WifiDetection;
import main.java.edu.pi.scanmap.util.AwsCredentials;
import main.java.edu.pi.scanmap.util.ConfigHelper;
import main.java.edu.pi.scanmap.util.FixedWindow;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.awt.Image;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ScanMapTool {

    private static Duration DETECTION_WAIT_TIME = Duration.ofSeconds(1);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final String IOT_ENDPOINT =
            ConfigHelper.getPropertyOrEnv("IOT_ENDPOINT",  "a131ws0b6gtght-ats.iot.us-east-1.amazonaws.com");

    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    private final FixedWindow<WifiDetection> wifiDetectionFixedWindow = new FixedWindow<>(WifiDetection.class, 10);
    private QRCodeManager qrCodeManager = new QRCodeManager();
    private WifiStrengthManager wifiStrengthManager = new WifiStrengthManager(threadPool);
    private MessageManager messageManager;
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

        final VideoCapture videoCapture = new VideoCapture((args.length ==1) ? Integer.parseInt(args[0]) : 0);
        final Consumer<Image> imageConsumer = image -> {
            ImageIcon icon = new ImageIcon(image);
            label.setIcon(icon);
            qrCode.setText(lastDetectedQRCode);
        };

        int errorCode = 0;
        try {
            final ScanMapTool scanMapTool = new ScanMapTool();

            System.out.println("Starting execution ...");
            scanMapTool.execute(videoCapture, imageConsumer);
        } catch (Throwable t) {
            t.printStackTrace();
            errorCode = -1;
        } finally {
            System.exit(errorCode);
        }
    }

    public ScanMapTool() throws Exception {
        messageManager =  new MessageManager(IOT_ENDPOINT, AwsCredentials.create());
        messageManager.publish(String.format("{\"event\":\"started\",\"timestamp\":%s}", System.currentTimeMillis()));
    }

    public void execute(final VideoCapture videoCapture, final Consumer<Image> imageConsumer) {
        threadPool.submit(new WifiStrengthDetectionTask());

        while (true) {
            try {
                final String qrCode = qrCodeManager.detectQRCode(videoCapture, imageConsumer);
                if (qrCode != null) {
                    lastDetectedQRCode = qrCode;
                    final long detectionTimestamp = System.currentTimeMillis();
                    Thread.sleep(DETECTION_WAIT_TIME.toMillis());
                    final WifiDetection lastWifiDetection = wifiDetectionFixedWindow.getItems().get(0);
                    final String qrCodeLocation = qrCode + ":" + lastWifiDetection;
                    messageManager.publishData(new ScanDetectionMessage(qrCode, detectionTimestamp, lastWifiDetection));
                    System.out.println("Detected qrCode: " + qrCodeLocation);
                }
            } catch (final Exception e) {
                e.printStackTrace();
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

    public static class ScanDetectionMessage {
        private final String qrCode;
        private final long timestamp;
        private final WifiDetection wifiDetection;

        public ScanDetectionMessage(String qrCode, long timestamp, WifiDetection wifiDetection) {
            this.qrCode = qrCode;
            this.timestamp = timestamp;
            this.wifiDetection = wifiDetection;
        }

        public String getQrCode() {
            return qrCode;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public WifiDetection getWifiDetection() {
            return wifiDetection;
        }
    }
}