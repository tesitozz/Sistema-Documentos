package org.documentos.documentos.controladores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller @RequestMapping("/secretaria/trabajadores")
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
            model.addAttribute("escuelas", escuelaRepo.findAll());
            return "secretaria/trabajador-form";
        }
        try {
            String temp = registroService.registrarTrabajador(form);
            ra.addFlashAttribute("ok",
                    "Trabajador creado correctamente. Contraseña temporal: " + temp);
            return "redirect:/secretaria/trabajadores";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("escuelas", escuelaRepo.findAll());
            return "secretaria/trabajador-form";
        }
    }

    @PostMapping("/secretaria/trabajadores/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            gestionTrabajadorService.desactivarTrabajador(id);
            ra.addFlashAttribute("ok", "Trabajador desactivado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo desactivar: " + e.getMessage());
        }
        return "redirect:/secretaria/trabajadores";
    }


    @PostMapping("/secretaria/trabajadores/{id}/reactivar")
    public String reactivar(@PathVariable Long id, RedirectAttributes ra) {
        try { gestionTrabajadorService.reactivarTrabajador(id); ra.addFlashAttribute("ok","Trabajador reactivado."); }
        catch (Exception e) { ra.addFlashAttribute("error","No se pudo reactivar: "+e.getMessage()); }
        return "redirect:/secretaria/trabajadores";
    }

}