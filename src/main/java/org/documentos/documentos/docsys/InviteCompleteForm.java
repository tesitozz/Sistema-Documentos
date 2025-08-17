package org.documentos.documentos.docsys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InviteCompleteForm {
    @NotBlank
    @Pattern(regexp="\\d{8}", message="DNI debe tener 8 dígitos")
    private String dni;
    @NotBlank private String nombres;
    @NotBlank private String apellidos;
    @NotNull
    private Long escuelaId;
    @NotBlank @Size(min=6, message="Contraseña mínimo 6 caracteres")
    private String password;
}
