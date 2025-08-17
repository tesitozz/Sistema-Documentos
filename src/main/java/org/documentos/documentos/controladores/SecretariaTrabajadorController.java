package org.documentos.documentos.controladores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.documentos.documentos.dto.TrabajadorForm;
import org.documentos.documentos.repositorios.EscuelaRepo;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.documentos.documentos.servicios.RegistroTrabajadorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/secretaria/trabajadores")
@RequiredArgsConstructor
public class SecretariaTrabajadorController {

    private final TrabajadorRepo trabajadorRepo;
    private final EscuelaRepo escuelaRepo;
    private final RegistroTrabajadorService registroService;
    private final org.documentos.documentos.servicios.GestionTrabajadorService gestionTrabajadorService;

    @GetMapping
    public String listar(@RequestParam(defaultValue="0") int page,
                         @RequestParam(defaultValue="10") int size,
                         Model model) {
        Page<?> lista = trabajadorRepo.findAllWithUsuarioEscuela(PageRequest.of(page, size));
        model.addAttribute("page", lista);
        return "secretaria/trabajadores"; // listado
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new TrabajadorForm());
        model.addAttribute("escuelas", escuelaRepo.findAll());
        return "secretaria/trabajador-form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") TrabajadorForm form,
                        BindingResult br,
                        Model model,
                        RedirectAttributes ra) {

        if (br.hasErrors()) {
            log.warn("Errores de validación al crear trabajador: {}", br.getAllErrors());
            model.addAttribute("escuelas", escuelaRepo.findAll());
            return "secretaria/trabajador-form";
        }

        try {
            log.info("Iniciando registro de trabajador: {} {} con DNI: {}",
                    form.getNombres(), form.getApellidos(), form.getDni());

            // USAR DNI COMO CONTRASEÑA
            String passwordDNI = registroService.registrarTrabajadorConDNI(form);

            log.info("Trabajador registrado exitosamente. Email: {}", form.getEmail());

            ra.addFlashAttribute("ok", String.format(
                    "✅ Trabajador '%s %s' creado correctamente.%n%n" +
                            "📧 Email: %s%n" +
                            "🔑 Contraseña: %s (su DNI)%n%n" +
                            "ℹ️ El trabajador debe usar su EMAIL y DNI para ingresar al sistema.",
                    form.getNombres(),
                    form.getApellidos(),
                    form.getEmail(),
                    passwordDNI
            ));

            return "redirect:/secretaria/trabajadores";

        } catch (IllegalArgumentException e) {
            log.error("Error de negocio al registrar trabajador: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("escuelas", escuelaRepo.findAll());
            return "secretaria/trabajador-form";

        } catch (Exception e) {
            log.error("Error inesperado al registrar trabajador", e);
            model.addAttribute("error", "Error inesperado: " + e.getMessage());
            model.addAttribute("escuelas", escuelaRepo.findAll());
            return "secretaria/trabajador-form";
        }
    }

    @PostMapping("/{id}/eliminar")  // Corregida la ruta
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            log.info("Desactivando trabajador con ID: {}", id);
            gestionTrabajadorService.desactivarTrabajador(id);
            ra.addFlashAttribute("ok", "Trabajador desactivado correctamente.");
        } catch (Exception e) {
            log.error("Error al desactivar trabajador ID {}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", "No se pudo desactivar: " + e.getMessage());
        }
        return "redirect:/secretaria/trabajadores";
    }

    @PostMapping("/{id}/reactivar")  // Corregida la ruta
    public String reactivar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            log.info("Reactivando trabajador con ID: {}", id);
            gestionTrabajadorService.reactivarTrabajador(id);
            ra.addFlashAttribute("ok", "Trabajador reactivado correctamente.");
        } catch (Exception e) {
            log.error("Error al reactivar trabajador ID {}: {}", id, e.getMessage());
            ra.addFlashAttribute("error", "No se pudo reactivar: " + e.getMessage());
        }
        return "redirect:/secretaria/trabajadores";
    }
}