package org.documentos.documentos.controladores;

import lombok.RequiredArgsConstructor;
import org.documentos.documentos.entidades.Documento;
import org.documentos.documentos.entidades.DocumentoHistorial;
import org.documentos.documentos.repositorios.DocumentoHistorialRepo;
import org.documentos.documentos.repositorios.DocumentoRepo;
import org.documentos.documentos.servicios.BizException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoRepo documentoRepo;
    private final DocumentoHistorialRepo historialRepo;

    @GetMapping("/documentos/{id}/historial")
    public String historial(@PathVariable Long id, Model model) {
        Documento d = documentoRepo.findById(id).orElseThrow(() -> new BizException("Documento no encontrado."));
        List<DocumentoHistorial> list = historialRepo.findByDocumentoOrderByFechaAsc(d);
        model.addAttribute("doc", d);
        model.addAttribute("historial", list);
        return "comun/historial"; // vista simple
    }

    @GetMapping("/documentos/{id}/descargar")
    public ResponseEntity<FileSystemResource> descargar(@PathVariable Long id) {
        Documento d = documentoRepo.findById(id).orElseThrow(() -> new BizException("Documento no encontrado."));
        File file = new File(d.getArchivoPath());
        if (!file.exists()) throw new BizException("Archivo no disponible.");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documento-" + d.getId() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }
}