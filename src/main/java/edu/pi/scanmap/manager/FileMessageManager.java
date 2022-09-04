package main.java.edu.pi.scanmap.manager;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class FileMessageManager extends MessageManager {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
    private static final String FILE_NAME_FORMAT = "ScanMap-%s.json";

    private File outputFile;

    public FileMessageManager(final File baseDir, final Instant timestamp) throws Exception {
        String formattedDate = DATE_FORMAT.format(new Date(timestamp.toEpochMilli()));
        outputFile = new File(baseDir, String.format(FILE_NAME_FORMAT, formattedDate));
        if (outputFile.createNewFile()) {
            System.out.println("Created file: " + outputFile.getAbsolutePath());
        } else {
            throw new Exception("Cannot create file: " + outputFile.getAbsolutePath());
        }
    }

    @Override
    public void publish(String message) throws Exception {
        final FileWriter fileWriter = new FileWriter(outputFile, true);;
        fileWriter.append(message);
        fileWriter.append("\n");
        fileWriter.flush();
    }
}
