package com.donations.donations.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StorageConfig {

    @Bean
    public FileStoragePort fileStoragePort(CloudflareR2Properties properties) {
        LocalStorageAdapter localAdapter = new LocalStorageAdapter(properties.getLocalUploadDir());

        if (properties.isConfigured()) {
            log.info("Cloudflare R2 ativado e configurado para o bucket: {}", properties.getBucketName());
            return new CloudflareR2StorageAdapter(properties, localAdapter);
        } else {
            log.warn("Cloudflare R2 não configurado ou desativado. A utilizar armazenamento local na pasta: {}", properties.getLocalUploadDir());
            return localAdapter;
        }
    }
}
