package com.donations.donations.service.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
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

    public boolean isConfigured() {
        return enabled &&
                accountId != null && !accountId.isBlank() &&
                accessKeyId != null && !accessKeyId.isBlank() &&
                secretAccessKey != null && !secretAccessKey.isBlank() &&
                bucketName != null && !bucketName.isBlank();
    }
}
