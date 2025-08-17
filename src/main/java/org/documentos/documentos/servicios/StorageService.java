package org.documentos.documentos.servicios;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    private final Path root;

    public StorageService(@Value("${app.storage.dir:./storage}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public String savePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BizException("Archivo PDF es requerido.");
        String ct = file.getContentType();
        if (ct == null || !ct.equalsIgnoreCase("application/pdf"))
            throw new BizException("Solo se permite PDF.");
        if (file.getOriginalFilename() != null && !file.getOriginalFilename().toLowerCase().endsWith(".pdf"))
            throw new BizException("El archivo debe tener extensión .pdf");

        String filename = UUID.randomUUID() + ".pdf";
        Path target = root.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString(); // guarda este path en Documento.archivoPath
        } catch (IOException e) {
            throw new BizException("No se pudo guardar el archivo: " + e.getMessage());
        }
    }
}