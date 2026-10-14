package com.donations.donations.service.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
public class LocalStorageAdapter implements FileStoragePort {

    private final Path rootDir;

    public LocalStorageAdapter(String uploadDir) {
        this.rootDir = Paths.get(uploadDir != null && !uploadDir.isBlank() ? uploadDir : "./uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
            log.info("Armazenamento local inicializado na pasta: {}", rootDir);
        } catch (IOException e) {
            log.error("Não foi possível criar a pasta de uploads local: {}", rootDir, e);
        }
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        String cleanKey = sanitizeKey(key);
        Path targetPath = rootDir.resolve(cleanKey).normalize();

        try {
            if (!targetPath.startsWith(rootDir)) {
                throw new SecurityException("Tentativa de acesso fora do diretório base");
            }
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content);
            log.info("Ficheiro guardado localmente: {}", cleanKey);
            return cleanKey;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao guardar ficheiro no disco local", e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration expiration) {
        // No armazenamento local, o ficheiro é servido via endpoint de media da aplicação
        return resolveUrl(key);
    }

    @Override
    public byte[] download(String key) {
        String cleanKey = sanitizeKey(key);
        Path targetPath = rootDir.resolve(cleanKey).normalize();

        try {
            if (!targetPath.startsWith(rootDir) || !Files.exists(targetPath)) {
                throw new RuntimeException("Ficheiro não encontrado no disco local: " + cleanKey);
            }
            return Files.readAllBytes(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler ficheiro do disco local", e);
        }
    }

    @Override
    public void delete(String key) {
        String cleanKey = sanitizeKey(key);
        Path targetPath = rootDir.resolve(cleanKey).normalize();

        try {
            if (targetPath.startsWith(rootDir)) {
                Files.deleteIfExists(targetPath);
            }
        } catch (IOException e) {
            log.warn("Erro ao eliminar ficheiro local: {}", cleanKey, e);
        }
    }

    @Override
    public String resolveUrl(String key) {
        String cleanKey = sanitizeKey(key);
        return "/api/public/media/" + cleanKey;
    }

    @Override
    public boolean isCloudflareR2() {
        return false;
    }

    public boolean exists(String key) {
        String cleanKey = sanitizeKey(key);
        Path targetPath = rootDir.resolve(cleanKey).normalize();
        return targetPath.startsWith(rootDir) && Files.exists(targetPath);
    }

    private String sanitizeKey(String key) {
        if (key == null) return "";
        String clean = key.trim();
        while (clean.startsWith("/")) {
            clean = clean.substring(1);
        }
        return clean.replace("..", "_");
    }
}
