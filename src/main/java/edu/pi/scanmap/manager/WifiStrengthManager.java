package main.java.edu.pi.scanmap.manager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class WifiStrengthManager {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private static final String[] LINE_FILTER = { "SSID", "Signal", "Address" };

    public void detectAccessPoints() throws Exception {

        ProcessBuilder builder = new ProcessBuilder();
        if (IS_WINDOWS) {
            builder.command("netsh", "wlan", "show", "networks", "mode=bssid");
        } else {
            builder.command("iwlist", "wlan0", "scan");
        }

        Process process = builder.start();
        final ProcessOutputHandler handler = new ProcessOutputHandler(process.getInputStream(), LINE_FILTER);
        Executors.newSingleThreadExecutor().submit(handler);
        int exitCode = process.waitFor();
        System.out.println("WIFI Process exit=" + exitCode);
        System.out.println(parseLines(handler.getOutputLines()));
    }

    private List<WifiStrength> parseLines(final List<String> lines) {
        final List<WifiStrength> wifiStrengthList = new ArrayList<>();
        int lineIndex = 0;
        while (lineIndex < lines.size()) {
            final WifiStrength wifiStrength =
                    IS_WINDOWS ? parseWindowsNetwork(lines, lineIndex) : parseUnixNetwork(lines, lineIndex);
            if (wifiStrength == null) {
                lineIndex++;
            } else {
                lineIndex += 3;
                wifiStrengthList.add(wifiStrength);
            }
        }

        return wifiStrengthList;
    }

    private WifiStrength parseWindowsNetwork(final List<String> lines, final int index) {
        if (index + 3 > lines.size()) {
            return null;
        }
        final String networkName = getValueAfter(lines.get(index), "SSID", ":");
        final String address = getValueAfter(lines.get(index + 1), "BSSID", ":");
        final String signal = getValueAfter(lines.get(index + 2), "Signal", ":");

        if ((networkName == null) || (address == null) || (signal == null)) {
            return null;
        }

        if (signal.endsWith("%")) {
            final double signalPercent = Double.parseDouble(signal.substring(0, signal.length() - 1));
            return new WifiStrength(networkName, address, signalPercent);
        }

        return null;
    }

    private WifiStrength parseUnixNetwork(final List<String> lines, final int index) {
        if (index + 3 > lines.size()) {
            return null;
        }
        String address = getValueAfter(lines.get(index), "Address:");
        final String signal = getValueAfter(lines.get(index + 1), "Quality=");
        final String ssid = getValueAfter(lines.get(index + 2), "ESSID:");

        if ((ssid == null) || (address == null) || (signal == null)) {
            return null;
        }

        // Strip quotes
        final String networkName = ssid.substring(1, ssid.length() - 1);

        final String signalRatio = signal.substring(0, signal.indexOf(" "));
        final String[] values = signalRatio.split("/");
        final double signalPercent = Double.parseDouble(values[0]) / Double.parseDouble(values[1]);
        return new WifiStrength(networkName, address, signalPercent);
    }

    private String getValueAfter(final String line, final String prefix, final String separator) {
        if (!line.startsWith(prefix)) {
            return null;
        }

        final int separatorIndex = line.indexOf(separator);
        if (separatorIndex != -1) {
            return line.substring(separatorIndex + 1).trim();
        }
        return null;
    }

    private String getValueAfter(final String line, final String separator) {
        final int separatorIndex = line.indexOf(separator);
        if (separatorIndex != -1) {
            return line.substring(separatorIndex + separator.length()).trim();
        }
        return null;
    }

    private static class ProcessOutputHandler implements Runnable {
        private final InputStream inputStream;
        private final String[] lineFilter;
        private final List<String> outputLines = new ArrayList<>();

        public ProcessOutputHandler(final InputStream inputStream, final String[] lineFilter) {
            this.inputStream = inputStream;
            this.lineFilter = lineFilter;
        }

        public List<String> getOutputLines() {
            return outputLines;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .filter(line -> filter(line))
                    .map(String::trim)
                    .forEach(line -> outputLines.add(line));
        }

        private boolean filter(final String line) {
            return Stream.of(lineFilter).anyMatch(l -> line.contains(l));
        }
    }

    public static class WifiStrength {
        private final String networkName;
        private final String address;
        private final double signalStrength;

        private WifiStrength(String networkName, String address, double signalStrength) {
            this.networkName = networkName;
            this.address = address;
            this.signalStrength = signalStrength;
        }

        public String getNetworkName() {
            return networkName;
        }

        public String getAddress() {
            return address;
        }

        public double getSignalStrength() {
            return signalStrength;
        }

        @Override
        public String toString() {
            return "WifiStrength{" +
                    "networkName='" + networkName + '\'' +
                    ", address='" + address + '\'' +
                    ", signalStrength=" + signalStrength +
                    '}';
        }
    }
}
