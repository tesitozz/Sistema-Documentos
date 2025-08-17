package org.documentos.documentos.controladores;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.documentos.documentos.servicios.RegistroTrabajadorService;
import org.springframework.web.bind.annotation.*;

/**
 * ⚠️ CONTROLADOR SOLO PARA DESARROLLO - ELIMINAR EN PRODUCCIÓN ⚠️
 * 
 * Endpoints para debugear problemas de contraseñas
 */
@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final RegistroTrabajadorService registroService;

    /**
     * Verificar si unas credenciales funcionan
     * GET /debug/verify?email=juan@test.com&password=12345678
     */
    @GetMapping("/verify")
    public String verificarCredenciales(@RequestParam String email, 
                                      @RequestParam String password) {
        try {
            boolean resultado = registroService.verificarCredenciales(email, password);
            return String.format("Verificación para %s: %s", email, resultado ? "✅ EXITOSA" : "❌ FALLIDA");
        } catch (Exception e) {
            log.error("Error verificando credenciales", e);
            return "❌ ERROR: " + e.getMessage();
        }
    }

    /**
     * Listar todos los trabajadores registrados
     * GET /debug/list-workers
     */
    @GetMapping("/list-workers")
    public String listarTrabajadores() {
        try {
            registroService.listarTrabajadoresConCredenciales();
            return "✅ Lista generada - revisar logs del servidor";
        } catch (Exception e) {
            log.error("Error listando trabajadores", e);
            return "❌ ERROR: " + e.getMessage();
        }
    }

    /**
     * Endpoint para probar el encoder directamente
     * GET /debug/test-encoder?password=12345678
     */
    @GetMapping("/test-encoder")
    public String testearEncoder(@RequestParam String password) {
        try {
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            String hash1 = encoder.encode(password);
            String hash2 = encoder.encode(password);
            
            boolean test1 = encoder.matches(password, hash1);
            boolean test2 = encoder.matches(password, hash2);
            
            return String.format(
                "Password: '%s'%n" +
                "Hash 1: %s%n" +
                "Hash 2: %s%n" +
                "Test 1: %s%n" +
                "Test 2: %s%n" +
                "Hashes iguales: %s",
                password, 
                hash1.substring(0, 30) + "...",
                hash2.substring(0, 30) + "...",
                test1 ? "✅" : "❌",
                test2 ? "✅" : "❌",
                hash1.equals(hash2) ? "SÍ (❌ malo)" : "NO (✅ bueno)"
            );
        } catch (Exception e) {
            return "❌ ERROR: " + e.getMessage();
        }
    }
}