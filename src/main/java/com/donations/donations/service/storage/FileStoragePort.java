package com.donations.donations.service.storage;

import java.time.Duration;

public interface FileStoragePort {

    /**
     * Uploads binary content to storage.
     *
     * @param key         the relative key/path (e.g. "avatars/creator-id/avatar.jpg")
     * @param content     file bytes
     * @param contentType MIME type (e.g. "image/jpeg", "image/png", "application/pdf")
     * @return the stored key
     */
    String upload(String key, byte[] content, String contentType);

    /**
     * Generates a temporary pre-signed URL for direct browser access.
     *
     * @param key        the object key
     * @param expiration duration until expiration
     * @return full pre-signed URL
     */
    String generatePresignedUrl(String key, Duration expiration);

    /**
     * Downloads raw bytes from storage.
     *
     * @param key the object key
     * @return raw bytes
     */
    byte[] download(String key);

    /**
     * Deletes an object from storage.
     *
     * @param key the object key
     */
    void delete(String key);

    /**
     * Resolves a public or access URL for the given key.
     * If a CDN/Public URL prefix is configured, returns public URL;
     * otherwise returns a pre-signed URL or proxy URL.
     *
     * @param key the object key
     * @return access URL
     */
    String resolveUrl(String key);

    /**
     * Checks if this storage implementation is active.
     */
    boolean isCloudflareR2();
}
