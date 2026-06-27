package com.uade.exammanager.infrastructure.adapter.out.storage;

import com.uade.exammanager.domain.port.out.HeaderImageStoragePort;
import com.uade.exammanager.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Adaptador de salida que guarda la imagen de cabecera en el sistema de archivos local.
 */
@Component
public class LocalHeaderImageStorageAdapter implements HeaderImageStoragePort {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String store(byte[] content, String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        String extension = cleaned.contains(".") ? cleaned.substring(cleaned.lastIndexOf('.')) : ".png";
        String storedName = "header-" + UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path target = uploadPath.resolve(storedName);
            Files.write(target, content);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen de cabecera.");
        }
    }
}
