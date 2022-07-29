package main.java.edu.pi.scanmap.manager;

import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.videoio.VideoCapture;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.function.Consumer;

public class QRCodeManager {

    private QRCodeDetector qrCodeDetector = new QRCodeDetector();

    public String detectQRCode(final VideoCapture videoCapture, final Consumer<Image> imageConsumer) {
        final Mat image = new Mat();
        boolean readStatus = videoCapture.read(image);
        //System.out.println("ReadStatus=" + readStatus);
        if (readStatus) {
            final Image displayImage = convertImage(image);
            imageConsumer.accept(displayImage);
            final String qrCode = qrCodeDetector.detectAndDecode(image);
            //System.out.println("QRCode=" + qrCode);
            return ((qrCode == null) || qrCode.isEmpty()) ? null : qrCode;
        } else {
            System.out.println("Read failed");
        }

        return null;
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
}
