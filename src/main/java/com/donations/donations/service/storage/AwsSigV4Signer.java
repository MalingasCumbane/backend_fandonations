package com.donations.donations.service.storage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AWS Signature Version 4 signer implementing S3 / Cloudflare R2 authentication.
 * Uses only standard Java 21 cryptography (HMAC-SHA256, SHA-256, HexFormat).
 */
public class AwsSigV4Signer {

    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String SERVICE = "s3";
    private static final String REGION = "auto";

    private static final DateTimeFormatter AMZ_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter DATE_STAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final String accessKey;
    private final String secretKey;

    public AwsSigV4Signer(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * Signs an HTTP request and returns required headers (Authorization, x-amz-date, x-amz-content-sha256).
     */
    public Map<String, String> signRequest(String method, String host, String canonicalUri, byte[] payload, Map<String, String> extraHeaders) {
        Instant now = Instant.now();
        String amzDate = AMZ_DATE_FORMAT.format(now);
        String dateStamp = DATE_STAMP_FORMAT.format(now);

        String payloadHash = HexFormat.of().formatHex(sha256(payload != null ? payload : new byte[0]));

        Map<String, String> headersToSign = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headersToSign.put("host", host);
        headersToSign.put("x-amz-date", amzDate);
        headersToSign.put("x-amz-content-sha256", payloadHash);
        if (extraHeaders != null) {
            extraHeaders.forEach((k, v) -> {
                if (v != null && !v.isBlank()) {
                    headersToSign.put(k.toLowerCase(Locale.ROOT), v.trim());
                }
            });
        }

        // Canonical headers & signed headers
        StringBuilder canonicalHeaders = new StringBuilder();
        StringBuilder signedHeaders = new StringBuilder();
        for (Map.Entry<String, String> entry : headersToSign.entrySet()) {
            String lowerKey = entry.getKey().toLowerCase(Locale.ROOT);
            canonicalHeaders.append(lowerKey).append(":").append(entry.getValue()).append("\n");
            if (signedHeaders.length() > 0) signedHeaders.append(";");
            signedHeaders.append(lowerKey);
        }

        String canonicalRequest = method + "\n"
                + canonicalUri + "\n"
                + "" + "\n" // canonical query string is empty for normal requests
                + canonicalHeaders
                + "\n"
                + signedHeaders
                + "\n"
                + payloadHash;

        String credentialScope = dateStamp + "/" + REGION + "/" + SERVICE + "/aws4_request";
        String stringToSign = ALGORITHM + "\n"
                + amzDate + "\n"
                + credentialScope + "\n"
                + HexFormat.of().formatHex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));

        byte[] signingKey = getSignatureKey(secretKey, dateStamp, REGION, SERVICE);
        String signature = HexFormat.of().formatHex(hmacSha256(signingKey, stringToSign));

        String authHeader = ALGORITHM + " "
                + "Credential=" + accessKey + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signature;

        Map<String, String> finalHeaders = new HashMap<>();
        finalHeaders.put("Authorization", authHeader);
        finalHeaders.put("x-amz-date", amzDate);
        finalHeaders.put("x-amz-content-sha256", payloadHash);
        return finalHeaders;
    }

    /**
     * Generates a Pre-Signed URL for GET requests (for temporary browser downloads/views).
     */
    public String generatePresignedUrl(String host, String bucketName, String key, Duration expiration) {
        Instant now = Instant.now();
        String amzDate = AMZ_DATE_FORMAT.format(now);
        String dateStamp = DATE_STAMP_FORMAT.format(now);
        long expiresSeconds = Math.max(60, Math.min(expiration.toSeconds(), 604800)); // between 1m and 7 days

        String canonicalUri = "/" + bucketName + "/" + encodePath(key);
        String credentialScope = dateStamp + "/" + REGION + "/" + SERVICE + "/aws4_request";

        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("X-Amz-Algorithm", ALGORITHM);
        queryParams.put("X-Amz-Credential", accessKey + "/" + credentialScope);
        queryParams.put("X-Amz-Date", amzDate);
        queryParams.put("X-Amz-Expires", String.valueOf(expiresSeconds));
        queryParams.put("X-Amz-SignedHeaders", "host");

        StringBuilder canonicalQuery = new StringBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (canonicalQuery.length() > 0) canonicalQuery.append("&");
            canonicalQuery.append(urlEncode(entry.getKey()))
                    .append("=")
                    .append(urlEncode(entry.getValue()));
        }

        String canonicalHeaders = "host:" + host + "\n";
        String signedHeaders = "host";
        String payloadHash = "UNSIGNED-PAYLOAD";

        String canonicalRequest = "GET\n"
                + canonicalUri + "\n"
                + canonicalQuery + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + payloadHash;

        String stringToSign = ALGORITHM + "\n"
                + amzDate + "\n"
                + credentialScope + "\n"
                + HexFormat.of().formatHex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));

        byte[] signingKey = getSignatureKey(secretKey, dateStamp, REGION, SERVICE);
        String signature = HexFormat.of().formatHex(hmacSha256(signingKey, stringToSign));

        return "https://" + host + canonicalUri + "?" + canonicalQuery + "&X-Amz-Signature=" + signature;
    }

    private static String encodePath(String path) {
        if (path == null) return "";
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(urlEncode(parts[i]));
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HmacSHA256 error", e);
        }
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) {
        try {
            byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
            byte[] kDate = hmacSha256(kSecret, dateStamp);
            byte[] kRegion = hmacSha256(kDate, regionName);
            byte[] kService = hmacSha256(kRegion, serviceName);
            return hmacSha256(kService, "aws4_request");
        } catch (Exception e) {
            throw new RuntimeException("Error deriving signature key", e);
        }
    }
}
