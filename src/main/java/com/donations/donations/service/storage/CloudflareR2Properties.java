package com.donations.donations.service.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage.cloudflare.r2")
public class CloudflareR2Properties {

    /**
     * Whether Cloudflare R2 is enabled.
     */
    private boolean enabled = false;

    /**
     * Cloudflare Account ID (found on Cloudflare dashboard home page or R2 overview).
     */
    private String accountId;

    /**
     * R2 API Token Access Key ID.
     */
    private String accessKeyId;

    /**
     * R2 API Token Secret Access Key.
     */
    private String secretAccessKey;

    /**
     * Bucket name in R2 (e.g. "txopa-media").
     */
    private String bucketName = "txopa-media";

    /**
     * Optional public URL prefix (e.g. "https://media.txopa.app" or "https://pub-xxxx.r2.dev").
     * When set, public assets like avatars will use this domain.
     */
    private String publicUrlPrefix;

    /**
     * Local storage fallback directory when R2 is disabled.
     */
    private String localUploadDir = "./uploads";

    public CloudflareR2Properties() {
    }

    public boolean isConfigured() {
        return enabled &&
                accountId != null && !accountId.isBlank() &&
                accessKeyId != null && !accessKeyId.isBlank() &&
                secretAccessKey != null && !secretAccessKey.isBlank() &&
                bucketName != null && !bucketName.isBlank();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPublicUrlPrefix() {
        return publicUrlPrefix;
    }

    public void setPublicUrlPrefix(String publicUrlPrefix) {
        this.publicUrlPrefix = publicUrlPrefix;
    }

    public String getLocalUploadDir() {
        return localUploadDir;
    }

    public void setLocalUploadDir(String localUploadDir) {
        this.localUploadDir = localUploadDir;
    }
}
