package com.donations.donations.controller;

import com.donations.donations.service.storage.FileStoragePort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/public/media")
@RequiredArgsConstructor
public class MediaController {

    private final FileStoragePort fileStoragePort;

    @GetMapping("/**")
    public ResponseEntity<?> getMedia(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        String prefix = "/api/public/media/";
        int idx = fullPath.indexOf(prefix);
        if (idx == -1) {
            return ResponseEntity.notFound().build();
        }
        String key = fullPath.substring(idx + prefix.length());
        if (key.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] bytes = fileStoragePort.download(key);
            MediaType mediaType = determineMediaType(key);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400, must-revalidate")
                    .contentType(mediaType)
                    .body(bytes);
        } catch (Exception e) {
            log.warn("Erro ao servir ficheiro de media: {}", key, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private MediaType determineMediaType(String key) {
        String lower = key.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
