package org.documentos.documentos.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "invitacion",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invitacion_token", columnNames = "token"),
                @UniqueConstraint(name = "uk_invitacion_email", columnNames = "email")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invitacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String token;

    @Column(nullable = false)
    private Instant expiracion;

    @Column(nullable = false)
    private boolean usado = false;
}
