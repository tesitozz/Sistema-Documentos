package org.documentos.documentos.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "trabajador",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_trabajador_dni", columnNames = "dni"),
                @UniqueConstraint(name = "uk_trabajador_usuario", columnNames = "usuario_id")
        },
        indexes = {
                @Index(name = "ix_trabajador_apellidos", columnList = "apellidos"),
                @Index(name = "ix_trabajador_dni", columnList = "dni")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trabajador {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1:1 con Usuario (rol TRABAJADOR)
    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_trabajador_usuario"))
    private Usuario usuario;

    @Pattern(regexp = "\\d{8}", message = "DNI debe tener 8 dígitos")
    @Column(nullable = false, length = 8)
    private String dni;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String apellidos;

    @ManyToOne(optional = false)
    @JoinColumn(name = "escuela_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_trabajador_escuela"))
    private Escuela escuela;
}
