package com.donations.donations.infrastructure.persistence.entity;

public class RequestContext {
    private static final ThreadLocal<String> currentIp = new ThreadLocal<>();

    public static void setIp(String ip) {
        currentIp.set(ip);
    }

    public static String getIp() {
        return currentIp.get();
    }

    public static void clear() {
        currentIp.remove();
    }
}
