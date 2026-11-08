package SmartWifiAnalyser.controller;

import SmartWifiAnalyser.service.WifiService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wifi")
@CrossOrigin(origins = "*")
public class WifiController {

    private final WifiService wifiService;

    // CONSTRUCTOR
    public WifiController(WifiService wifiService) {

        this.wifiService = wifiService;
    }


    // CURRENT WIFI DETAILS
    @GetMapping
    public Map<String, String> getWifiDetails() {

        return wifiService.getWifiDetails();
    }


    // NEARBY WIFI NETWORKS
    @GetMapping("/nearby")
    public List<Map<String, String>> getNearbyNetworks() {

        return wifiService.getNearbyNetworks();
    }

}