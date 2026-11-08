package SmartWifiAnalyser.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class WifiService {

    // GET CURRENT WIFI DETAILS
    public Map<String, String> getWifiDetails() {

        Map<String, String> wifi = new LinkedHashMap<>();

        wifi.put("status", "Disconnected");
        wifi.put("ssid", "Not Connected");
        wifi.put("signal", "0%");
        wifi.put("channel", "N/A");
        wifi.put("standard", "N/A");
        wifi.put("security", "N/A");
        wifi.put("receiveRate", "N/A");
        wifi.put("transmitRate", "N/A");
        wifi.put("band", "N/A");

        try {

            ProcessBuilder builder = new ProcessBuilder(
                "netsh", "wlan", "show", "interfaces"
            );

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.startsWith("SSID")
                        && !line.startsWith("BSSID")) {

                    wifi.put("ssid", getValue(line));
                    wifi.put("status", "Connected");
                }

                else if (line.startsWith("Signal")) {

                    wifi.put("signal", getValue(line));
                }

                else if (line.startsWith("Channel")) {

                    String channel = getValue(line);

                    wifi.put("channel", channel);

                    try {
                        int ch = Integer.parseInt(channel);

                        wifi.put("band",
                            ch <= 14 ? "2.4 GHz" : "5 GHz");

                    } catch (Exception ignored) {
                    }
                }

                else if (line.startsWith("Radio type")) {

                    wifi.put("standard", getValue(line));
                }

                else if (line.startsWith("Authentication")) {

                    wifi.put("security", getValue(line));
                }

                else if (line.startsWith("Receive rate")) {

                    wifi.put("receiveRate", getValue(line));
                }

                else if (line.startsWith("Transmit rate")) {

                    wifi.put("transmitRate", getValue(line));
                }
            }

            process.waitFor();

        } catch (Exception e) {

            wifi.put("error", e.getMessage());
        }

        return wifi;
    }


    // GET NEARBY WIFI NETWORKS
    public List<Map<String, String>> getNearbyNetworks() {

        List<Map<String, String>> networks = new ArrayList<>();

        try {

            ProcessBuilder builder = new ProcessBuilder(
                "netsh",
                "wlan",
                "show",
                "networks",
                "mode=bssid"
            );

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;

            Map<String, String> currentNetwork = null;
            Map<String, String> currentAccessPoint = null;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                // SSID
                if (line.matches("SSID\\s+\\d+\\s*:.*")) {

                    currentNetwork = new LinkedHashMap<>();

                    currentNetwork.put("ssid", getValue(line));
                    currentNetwork.put("security", "Unknown");
                    currentNetwork.put("signal", "Unknown");
                    currentNetwork.put("channel", "Unknown");

                    currentAccessPoint = null;
                }

                // SECURITY
                else if (currentNetwork != null
                        && line.startsWith("Authentication")) {

                    currentNetwork.put(
                        "security", getValue(line)
                    );
                }

                // BSSID
                else if (currentNetwork != null
                        && line.matches("BSSID\\s+\\d+\\s*:.*")) {

                    currentAccessPoint =
                        new LinkedHashMap<>(currentNetwork);

                    currentAccessPoint.put(
                        "bssid", getValue(line)
                    );

                    currentAccessPoint.put("signal", "Unknown");
                    currentAccessPoint.put("channel", "Unknown");

                    networks.add(currentAccessPoint);
                }

                // SIGNAL
                else if (currentAccessPoint != null
                        && line.startsWith("Signal")) {

                    currentAccessPoint.put(
                        "signal", getValue(line)
                    );
                }

                // CHANNEL
                else if (currentAccessPoint != null
                        && line.startsWith("Channel")) {

                    currentAccessPoint.put(
                        "channel", getValue(line)
                    );
                }
            }

            process.waitFor();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return networks;
    }


    // EXTRACT VALUE AFTER :
    private String getValue(String line) {

        int index = line.indexOf(":");

        if (index >= 0) {

            return line.substring(index + 1).trim();
        }

        return "N/A";
    }
}