package org.documentos.documentos.servicios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroTrabajadorService {

    private final UsuarioRepo usuarioRepo;
    private final TrabajadorRepo trabajadorRepo;
    private final EscuelaRepo escuelaRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public String registrarTrabajadorConDNI(TrabajadorForm f) {
        // Validaciones de negocio
        usuarioRepo.findByEmail(f.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Ya existe un usuario con ese email: " + f.getEmail());
        });
        trabajadorRepo.findByDni(f.getDni()).ifPresent(t -> {
            throw new IllegalArgumentException("Ya existe un trabajador con ese DNI: " + f.getDni());
        });

        Escuela esc = escuelaRepo.findById(f.getEscuelaId())
                .orElseThrow(() -> new IllegalArgumentException("Escuela no encontrada con ID: " + f.getEscuelaId()));

        // USAR EL DNI COMO CONTRASEÑA
        String passwordDNI = f.getDni();

        log.info("=== REGISTRO DE TRABAJADOR ===");
        log.info("Email: {}", f.getEmail());
        log.info("DNI: {}", f.getDni());
        log.info("Password a usar (DNI): '{}'", passwordDNI);
        log.info("Longitud del DNI: {}", passwordDNI.length());
        log.info("Caracteres del DNI: {}", passwordDNI.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .toArray());

        // Hashear el DNI
        String hashedPassword = encoder.encode(passwordDNI);
        log.info("Password hasheado correctamente");
        log.info("Hash generado (primeros 20 chars): {}", hashedPassword.substring(0, Math.min(20, hashedPassword.length())));

        // Verificar inmediatamente que el hash funciona
        boolean verificacion = encoder.matches(passwordDNI, hashedPassword);
        log.info("Verificación inmediata del hash: {}", verificacion ? "✅ EXITOSA" : "❌ FALLIDA");

        if (!verificacion) {
            log.error("❌ ERROR CRÍTICO: El hash no coincide inmediatamente después de generarlo");
            throw new RuntimeException("Error en el encoder de contraseñas");
        }

        Usuario u = Usuario.builder()
                .email(f.getEmail().trim().toLowerCase())
                .passwordHash(hashedPassword)
                .rol(Rol.TRABAJADOR)
                .activo(true)
                .build();
        u = usuarioRepo.save(u);

        log.info("Usuario guardado con ID: {}", u.getId());

        Trabajador t = Trabajador.builder()
                .usuario(u)
                .dni(f.getDni())
                .nombres(f.getNombres())
                .apellidos(f.getApellidos())
                .escuela(esc)
                .build();
        t = trabajadorRepo.save(t);

        log.info("Trabajador guardado con ID: {}", t.getId());
        log.info("=== FIN REGISTRO ===");

        return passwordDNI; // Devolver el DNI como contraseña
    }

    /**
     * Método para verificar contraseñas después del registro
     */
    public boolean verificarCredenciales(String email, String passwordIngresada) {
        log.info("=== VERIFICACIÓN DE CREDENCIALES ===");
        log.info("Email a verificar: '{}'", email);
        log.info("Password ingresada: '{}'", passwordIngresada);
        log.info("Longitud password: {}", passwordIngresada.length());

        Usuario usuario = usuarioRepo.findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (usuario == null) {
            log.error("❌ Usuario no encontrado para email: {}", email);
            return false;
        }

        log.info("✅ Usuario encontrado - ID: {}, Activo: {}, Rol: {}",
                usuario.getId(), usuario.isActivo(), usuario.getRol());

        boolean matches = encoder.matches(passwordIngresada, usuario.getPasswordHash());
        log.info("Resultado verificación: {}", matches ? "✅ EXITOSA" : "❌ FALLIDA");

        if (!matches) {
            log.info("Hash almacenado (primeros 20 chars): {}",
                    usuario.getPasswordHash().substring(0, Math.min(20, usuario.getPasswordHash().length())));

            // Probar variaciones comunes
            log.info("--- Probando variaciones ---");
            log.info("Password con espacios adelante/atrás: '{}'", passwordIngresada.trim());
            log.info("Matches con trim: {}", encoder.matches(passwordIngresada.trim(), usuario.getPasswordHash()));
        }

        log.info("=== FIN VERIFICACIÓN ===");
        return matches;
    }

    /**
     * Listar todos los trabajadores con sus datos de login
     */
    public void listarTrabajadoresConCredenciales() {
        log.info("=== LISTADO DE TRABAJADORES Y CREDENCIALES ===");

        trabajadorRepo.findAll().forEach(trabajador -> {
            Usuario usuario = trabajador.getUsuario();
            log.info("ID: {} | DNI: {} | Email: {} | Nombres: {} {} | Activo: {}",
                    trabajador.getId(),
                    trabajador.getDni(),
                    usuario.getEmail(),
                    trabajador.getNombres(),
                    trabajador.getApellidos(),
                    usuario.isActivo()
            );
        });

        log.info("=== FIN LISTADO ===");
    }
}