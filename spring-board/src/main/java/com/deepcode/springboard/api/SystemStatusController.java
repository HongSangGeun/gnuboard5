package com.deepcode.springboard.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SystemStatusController {

    @GetMapping("/api/system_status")
    public Map<String, String> systemStatus() {
        Map<String, String> result = new HashMap<>();

        result.put("cpu", cpuLoad());
        result.put("memory", memoryUsage());
        result.put("disk", diskUsage());

        return result;
    }

    private String cpuLoad() {
        try {
            com.sun.management.OperatingSystemMXBean os =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double load = os.getSystemCpuLoad();
            if (load < 0) {
                return "N/A";
            }
            return String.format("%.2f%%", load * 100.0);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String memoryUsage() {
        try {
            com.sun.management.OperatingSystemMXBean os =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long total = os.getTotalPhysicalMemorySize();
            long free = os.getFreePhysicalMemorySize();
            if (total <= 0) {
                return "N/A";
            }
            double used = 1.0 - ((double) free / (double) total);
            return String.format("%.2f%%", used * 100.0);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String diskUsage() {
        try {
            File root = new File("/");
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            if (total <= 0) {
                return "N/A";
            }
            double used = (double) (total - free) / (double) total;
            return String.format("%.2f%%", used * 100.0);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
