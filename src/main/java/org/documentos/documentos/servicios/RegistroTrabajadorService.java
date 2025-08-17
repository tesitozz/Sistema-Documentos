package org.documentos.documentos.servicios;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.documentos.documentos.dto.TrabajadorForm;
import org.documentos.documentos.entidades.Escuela;
import org.documentos.documentos.entidades.Trabajador;
import org.documentos.documentos.entidades.Usuario;
import org.documentos.documentos.entidades.enums.Rol;
import org.documentos.documentos.repositorios.EscuelaRepo;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.documentos.documentos.repositorios.UsuarioRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistroTrabajadorService {

    private final UsuarioRepo usuarioRepo;
    private final TrabajadorRepo trabajadorRepo;
    private final EscuelaRepo escuelaRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public String registrarTrabajador(TrabajadorForm f) {
        // Validaciones negocio
        usuarioRepo.findByEmail(f.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        });
        trabajadorRepo.findByDni(f.getDni()).ifPresent(t -> {
            throw new IllegalArgumentException("Ya existe un trabajador con ese DNI");
        });

        Escuela esc = escuelaRepo.findById(f.getEscuelaId())
                .orElseThrow(() -> new IllegalArgumentException("Escuela no encontrada"));

        // Genera password temporal (puedes reemplazar por un generador)
        String plainTemp = "Cambiar123!"; // en prod: genera aleatorio + enviar por correo

        Usuario u = Usuario.builder()
                .email(f.getEmail().trim().toLowerCase())
                .passwordHash(encoder.encode(plainTemp))
                .rol(Rol.TRABAJADOR)
                .activo(true)
                .build();
        u = usuarioRepo.save(u);

        Trabajador t = Trabajador.builder()
                .usuario(u)
                .dni(f.getDni())
                .nombres(f.getNombres())
                .apellidos(f.getApellidos())
                .escuela(esc)
                .build();
        trabajadorRepo.save(t);

        return plainTemp; // para mostrar una sola vez en pantalla
    }
}