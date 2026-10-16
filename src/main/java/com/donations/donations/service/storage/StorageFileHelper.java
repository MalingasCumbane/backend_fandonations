package com.donations.donations.service.storage;

import java.util.Base64;

public final class StorageFileHelper {

    private StorageFileHelper() {
    }

    public static boolean isDataUrlOrBase64(String input) {
        if (input == null || input.isBlank()) return false;
        String trimmed = input.trim();
        return trimmed.startsWith("data:") || trimmed.length() > 500 && !trimmed.startsWith("http://") && !trimmed.startsWith("https://") && !trimmed.startsWith("/");
    }

    public static byte[] extractBytes(String input) {
        if (input == null) return new byte[0];
        String trimmed = input.trim();
        if (trimmed.contains(",")) {
            // Strip data:image/...;base64, prefix
            String base64Data = trimmed.substring(trimmed.indexOf(",") + 1);
            return Base64.getDecoder().decode(base64Data);
        }
        return Base64.getDecoder().decode(trimmed);
    }

    public static String extractMimeType(String input, String fallback) {
        if (input != null && input.startsWith("data:")) {
            int semicolon = input.indexOf(";");
            if (semicolon > 5) {
                return input.substring(5, semicolon);
            }
        }
        return fallback != null && !fallback.isBlank() ? fallback : "application/octet-stream";
    }

    public static String getExtensionFromMime(String mimeType) {
        if (mimeType == null) return "bin";
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "application/pdf" -> "pdf";
            default -> "bin";
        };
    }
}
