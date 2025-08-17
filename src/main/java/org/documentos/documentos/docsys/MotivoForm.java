package org.documentos.documentos.docsys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MotivoForm {
    @NotBlank
    private String motivo;
}

