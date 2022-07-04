package edu.pi.scanmap;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ScanMapTool {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private QRCodeDetector qrCodeDetector = new QRCodeDetector();
    private JLabel label;

    public static void main(final String[] args) {
        System.out.println("Starting ScanMap");

        final JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(800, 700);
        JLabel label = new JLabel();
        frame.add(label);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final VideoCapture videoCapture = new VideoCapture(0);
        final ScanMapTool scanMapTool = new ScanMapTool(label);
        while (true) {
            if (scanMapTool.detectQRCodes(videoCapture)) {
                break;
            }
        }

        System.exit(0);
    }

    public ScanMapTool(JLabel label) {
        this.label = label;
    }

    private boolean detectQRCodes(final VideoCapture videoCapture) {
        final Mat image = new Mat();
        boolean readStatus = videoCapture.read(image);
        System.out.println("ReadStatus=" + readStatus);
        if (readStatus) {
            final Image displayImage = convertImage(image);
            displayImage(displayImage);
            final String qrCode = qrCodeDetector.detectAndDecode(image);
            System.out.println("QRCode=" + qrCode);
            boolean hasQrCode = (qrCode != null) && !qrCode.isEmpty();
            return hasQrCode;
        }

        return false;
    }

    private Image convertImage(final Mat m)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1)
        {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    public void displayImage(final Image img)
    {
        ImageIcon icon = new ImageIcon(img);
        label.setIcon(icon);
    }
}