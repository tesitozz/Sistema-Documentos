package org.documentos.documentos.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.documentos.documentos.entidades.enums.EstadoDocumento;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "documento",
        indexes = {
                @Index(name = "ix_documento_estado", columnList = "estado"),
                @Index(name = "ix_documento_creado_en", columnList = "creado_en"),
                @Index(name = "ix_documento_trabajador", columnList = "trabajador_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Documento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trabajador_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_documento_trabajador"))
    private Trabajador trabajador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_documento_tipo"))
    private TipoDocumento tipo;

    @NotBlank
    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(length = 2000)
    private String resumen;

    // Ruta o key del archivo PDF en el storage
    @NotBlank
    @Column(name = "archivo_path", nullable = false, length = 300)
    private String archivoPath;

    @Column(nullable = false)
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDocumento estado;

    // Quién tiene la pelota: SECRETARIA / JEFA / null
    @Column(name = "responsable_actual", length = 20)
    private String responsableActual;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;
}