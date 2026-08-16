package com.wikify.media;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FilesystemStorage implements MediaStorage {

    private final Path root;

    public FilesystemStorage(@Value("${wikify.media.root}") String root) throws IOException {
        this.root = Path.of(root).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    public String store(InputStream content, String contentType) {
        String id = UUID.randomUUID().toString();

        String key = id.substring(0, 2) + "/" + id.substring(2, 4) + "/" + id;

        Path destination = resolver(key);
        try {
            Files.createDirectories(destination.getParent());
            Path temp = Files.createTempFile(destination.getParent(), "upload-", ".tmp");
            try {
                Files.copy(content, temp, StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Files.deleteIfExists(temp);
                throw e;
            }
            return key;

        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar a mídia", e);
        }
    }

    @Override
    public Resource open(String storageKey) {
        Path file = resolver(storageKey);
        if (!Files.isReadable(file)) {
            throw new EntityNotFoundException("Mídia não encontrada");
        }
        return new FileSystemResource(file);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolver(storageKey));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String providerName() {
        return "local";
    }


    private Path resolver(String storageKey) {
        Path path = root.resolve(storageKey).normalize();

        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Chave de mídia inválida");
        }
        return path;
    }
}
