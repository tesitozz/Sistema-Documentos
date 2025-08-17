package org.documentos.documentos.docsys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentoForm {
    @NotNull
    private Long tipoId;
    @NotBlank
    private String titulo;
    private String resumen;
    @NotNull private MultipartFile archivo; // validar PDF en servicio
}