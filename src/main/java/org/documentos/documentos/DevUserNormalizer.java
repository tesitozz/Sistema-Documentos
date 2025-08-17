package org.documentos.documentos;

import org.documentos.documentos.entidades.Usuario;
import org.documentos.documentos.entidades.enums.Rol;
import org.documentos.documentos.repositorios.UsuarioRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev") // <- solo en dev
public class DevUserNormalizer {

    private static boolean looksLikeBcrypt(String hash) {
        if (hash == null) return false;
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    @Bean
    CommandLineRunner normalizeAndSeed(UsuarioRepo repo, PasswordEncoder enc) {
        return args -> {
            // 1) Normaliza TODOS los usuarios existentes
            int fixed = 0;
            for (Usuario u : repo.findAll()) {
                if (!looksLikeBcrypt(u.getPasswordHash())) {
                    u.setPasswordHash(enc.encode("Cambiar123!")); // temp en dev
                    u.setActivo(true);
                    repo.save(u);
                    fixed++;
                    System.out.println("[NORM] " + u.getEmail() + " -> BCrypt (temp: Cambiar123!)");
                }
            }
            if (fixed > 0) System.out.println("[NORM] Usuarios normalizados: " + fixed);

            // 2) Asegura los 3 usuarios demo (todos con 12345678, BCrypt)
            seedIfMissing(repo, enc, "secretaria@demo.com", Rol.SECRETARIA);
            seedIfMissing(repo, enc, "jefa@demo.com",       Rol.JEFA);
            seedIfMissing(repo, enc, "trabajador@demo.com", Rol.TRABAJADOR);
        };
    }

    private void seedIfMissing(UsuarioRepo repo, PasswordEncoder enc, String email, Rol rol) {
        repo.findByEmail(email).ifPresentOrElse(u -> {
            u.setRol(rol);
            u.setActivo(true);
            u.setPasswordHash(enc.encode("12345678"));
            repo.save(u);
            System.out.println("[SEED] Reset " + email + " / 12345678 (BCrypt)");
        }, () -> {
            Usuario u = Usuario.builder()
                    .email(email)
                    .passwordHash(enc.encode("12345678"))
                    .rol(rol)
                    .activo(true)
                    .build();
            repo.save(u);
            System.out.println("[SEED] Creado " + email + " / 12345678 (BCrypt)");
        });
    }
}