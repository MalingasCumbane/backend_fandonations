package com.donations.donations.service.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class CloudflareR2StorageAdapter implements FileStoragePort {

    private final CloudflareR2Properties properties;
    private final AwsSigV4Signer signer;
    private final HttpClient httpClient;
    private final String host;
    private final LocalStorageAdapter localFallback;

    public CloudflareR2StorageAdapter(CloudflareR2Properties properties, LocalStorageAdapter localFallback) {
        this.properties = properties;
        this.localFallback = localFallback;
        this.host = properties.getAccountId() + ".r2.cloudflarestorage.com";
        this.signer = new AwsSigV4Signer(properties.getAccessKeyId(), properties.getSecretAccessKey());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        String cleanKey = sanitizeKey(key);
        String canonicalUri = "/" + properties.getBucketName() + "/" + cleanKey;
        String requestUrl = "https://" + host + canonicalUri;

        Map<String, String> signedHeaders = signer.signRequest(
                "PUT",
                host,
                canonicalUri,
                content,
                Map.of("Content-Type", contentType != null ? contentType : "application/octet-stream")
        );

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(content));

        signedHeaders.forEach(builder::header);

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Ficheiro carregado com sucesso para Cloudflare R2: {}", cleanKey);
                return cleanKey;
            } else {
                log.error("Cloudflare R2 rejeitou o upload (HTTP {}): {}. A usar contingência local para {}",
                        response.statusCode(), response.body(), cleanKey);
                return localFallback.upload(cleanKey, content, contentType);
            }
        } catch (Exception e) {
            log.error("Erro ao comunicar com Cloudflare R2: {}. A usar contingência local para {}",
                    e.getMessage(), cleanKey);
            return localFallback.upload(cleanKey, content, contentType);
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration expiration) {
        String cleanKey = sanitizeKey(key);
        if (localFallback != null && localFallback.exists(cleanKey)) {
            return localFallback.generatePresignedUrl(cleanKey, expiration);
        }
        return signer.generatePresignedUrl(host, properties.getBucketName(), cleanKey, expiration);
    }

    @Override
    public byte[] download(String key) {
        String cleanKey = sanitizeKey(key);
        if (localFallback != null && localFallback.exists(cleanKey)) {
            return localFallback.download(cleanKey);
        }

        String canonicalUri = "/" + properties.getBucketName() + "/" + cleanKey;
        String requestUrl = "https://" + host + canonicalUri;

        Map<String, String> signedHeaders = signer.signRequest(
                "GET",
                host,
                canonicalUri,
                new byte[0],
                null
        );

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(30))
                .GET();

        signedHeaders.forEach(builder::header);

        try {
            HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("Ficheiro não encontrado no Cloudflare R2: HTTP " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao descarregar ficheiro do Cloudflare R2", e);
        }
    }

    @Override
    public void delete(String key) {
        String cleanKey = sanitizeKey(key);
        if (localFallback != null && localFallback.exists(cleanKey)) {
            localFallback.delete(cleanKey);
        }

        String canonicalUri = "/" + properties.getBucketName() + "/" + cleanKey;
        String requestUrl = "https://" + host + canonicalUri;

        Map<String, String> signedHeaders = signer.signRequest(
                "DELETE",
                host,
                canonicalUri,
                new byte[0],
                null
        );

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofSeconds(15))
                .DELETE();

        signedHeaders.forEach(builder::header);

        try {
            httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log.warn("Erro ao remover ficheiro do Cloudflare R2: {}", cleanKey, e);
        }
    }

    @Override
    public String resolveUrl(String key) {
        String cleanKey = sanitizeKey(key);
        if (localFallback != null && localFallback.exists(cleanKey)) {
            return localFallback.resolveUrl(cleanKey);
        }

        if (properties.getPublicUrlPrefix() != null && !properties.getPublicUrlPrefix().isBlank()) {
            String prefix = properties.getPublicUrlPrefix().trim();
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            return prefix + "/" + cleanKey;
        }
        return generatePresignedUrl(cleanKey, Duration.ofHours(24));
    }

    @Override
    public boolean isCloudflareR2() {
        return true;
    }

    private String sanitizeKey(String key) {
        if (key == null) return "";
        String clean = key.trim();
        while (clean.startsWith("/")) {
            clean = clean.substring(1);
        }
        return clean;
    }
}
