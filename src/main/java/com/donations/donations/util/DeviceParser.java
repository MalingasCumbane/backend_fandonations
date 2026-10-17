package com.donations.donations.util;

public class DeviceParser {

    public static String parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Dispositivo Web";
        }

        String ua = userAgent.toLowerCase();

        // 1. Detect Browser
        String browser = "Navegador";
        if (ua.contains("edg/") || ua.contains("edge/")) {
            browser = "Edge";
        } else if (ua.contains("opr/") || ua.contains("opera/")) {
            browser = "Opera";
        } else if (ua.contains("chrome/") && !ua.contains("edg/") && !ua.contains("opr/")) {
            browser = "Chrome";
        } else if (ua.contains("firefox/")) {
            browser = "Firefox";
        } else if (ua.contains("safari/") && !ua.contains("chrome/")) {
            browser = "Safari";
        } else if (ua.contains("samsungbrowser/")) {
            browser = "Samsung Internet";
        }

        // 2. Detect OS / Device
        String os = "Desktop";
        if (ua.contains("iphone")) {
            os = "iPhone";
        } else if (ua.contains("ipad")) {
            os = "iPad";
        } else if (ua.contains("android")) {
            os = "Android";
        } else if (ua.contains("macintosh") || ua.contains("mac os x")) {
            os = "macOS";
        } else if (ua.contains("windows")) {
            os = "Windows";
        } else if (ua.contains("linux")) {
            os = "Linux";
        }

        return browser + " no " + os;
    }
}
