package org.documentos.documentos.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrabajadorForm {
    @NotBlank
    @Size(max=80) private String nombres;
    @NotBlank @Size(max=80) private String apellidos;
    @NotBlank @Pattern(regexp="\\d{8}", message="DNI debe tener 8 dígitos") private String dni;
    @NotBlank @Email
    @Size(max=120) private String email;
    @NotNull
    private Long escuelaId;
}
