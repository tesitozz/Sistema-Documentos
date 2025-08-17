package org.documentos.documentos.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.documentos.documentos.entidades.enums.EstadoDocumento;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "documento_historial",
        indexes = {
                @Index(name = "ix_historial_doc", columnList = "documento_id"),
                @Index(name = "ix_historial_fecha", columnList = "fecha")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoHistorial {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "documento_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_historial_documento"))
    private Documento documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDocumento estado;

    // Usuario que realizó la acción (secretaria/jefa/trabajador)
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_historial_usuario"))
    private Usuario usuario;

    // Obligatorio cuando sea un rechazo
    @Column(length = 2000)
    private String motivo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant fecha;
}
