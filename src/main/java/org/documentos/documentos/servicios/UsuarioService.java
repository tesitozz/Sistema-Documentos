package org.documentos.documentos.servicios;

import org.documentos.documentos.entidades.Escuela;
import org.documentos.documentos.entidades.Invitacion;
import org.documentos.documentos.entidades.Trabajador;
import org.documentos.documentos.entidades.Usuario;
import org.documentos.documentos.entidades.enums.Rol;
import org.documentos.documentos.repositorios.EscuelaRepo;
import org.documentos.documentos.repositorios.InvitacionRepo;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.documentos.documentos.repositorios.UsuarioRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepo usuarioRepo;
    private final TrabajadorRepo trabajadorRepo;
    private final EscuelaRepo escuelaRepo;
    private final InvitacionRepo invitacionRepo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepo usuarioRepo, TrabajadorRepo trabajadorRepo,
                          EscuelaRepo escuelaRepo, InvitacionRepo invitacionRepo,
                          PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.trabajadorRepo = trabajadorRepo;
        this.escuelaRepo = escuelaRepo;
        this.invitacionRepo = invitacionRepo;
        this.encoder = encoder;
    }

    // Creada por secretaria: preregistra email + genera link
    @Transactional
    public Invitacion crearInvitacion(String email) {
        usuarioRepo.findByEmail(email).ifPresent(u -> { throw new BizException("Email ya está registrado."); });
        invitacionRepo.findByEmail(email).ifPresent(i -> { throw new BizException("Ya existe invitación pendiente."); });
        Invitacion inv = Invitacion.builder()
                .email(email)
                .token(UUID.randomUUID().toString())
                .expiracion(Instant.now().plus(3, ChronoUnit.DAYS))
                .usado(false)
                .build();
        return invitacionRepo.save(inv);
    }

    // Usada por el trabajador: crea usuario y trabajador
    @Transactional
    public Usuario completarRegistroConInvitacion(String token, String password, String dni,
                                                  String nombres, String apellidos, Long escuelaId) {
        Invitacion inv = invitacionRepo.findByToken(token)
                .orElseThrow(() -> new BizException("Invitación no válida."));
        if (inv.isUsado()) throw new BizException("Invitación ya utilizada.");
        if (Instant.now().isAfter(inv.getExpiracion())) throw new BizException("Invitación expirada.");

        if (usuarioRepo.findByEmail(inv.getEmail()).isPresent())
            throw new BizException("Email ya registrado.");
        if (trabajadorRepo.findByDni(dni).isPresent())
            throw new BizException("DNI ya registrado.");

        Usuario u = Usuario.builder()
                .email(inv.getEmail())
                .passwordHash(encoder.encode(password))
                .rol(Rol.TRABAJADOR)
                .activo(true)
                .build();
        u = usuarioRepo.save(u);

        Escuela esc = escuelaRepo.findById(escuelaId)
                .orElseThrow(() -> new BizException("Escuela no encontrada."));
        Trabajador t = Trabajador.builder()
                .usuario(u)
                .dni(dni)
                .nombres(nombres)
                .apellidos(apellidos)
                .escuela(esc)
                .build();
        trabajadorRepo.save(t);

        inv.setUsado(true);
        invitacionRepo.save(inv);
        return u;
    }
}