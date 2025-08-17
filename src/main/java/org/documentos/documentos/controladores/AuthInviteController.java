package org.documentos.documentos.controladores;

import org.documentos.documentos.docsys.InviteCompleteForm;
import org.documentos.documentos.entidades.Invitacion;
import org.documentos.documentos.repositorios.EscuelaRepo;
import org.documentos.documentos.servicios.BizException;
import org.documentos.documentos.servicios.UsuarioService;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthInviteController {

    private final UsuarioService usuarioService;
    private final EscuelaRepo escuelaRepo;

    // Página de login (configurada en SecurityConfig)
    @GetMapping("/login")
    public String login() {
        return "auth/login"; // templates/auth/login.html
    }

    // --- SECRETARIA crea invitación ---
    @PostMapping("/secretaria/invitaciones")
    public String crearInvitacion(@RequestParam String email,
                                  Model model) {
        Invitacion inv = usuarioService.crearInvitacion(email);
        model.addAttribute("ok", "Invitación creada. Token: " + inv.getToken());
        return "redirect:/secretaria/revision";
    }

    // --- TRABAJADOR completa registro desde token ---
    @GetMapping("/invite/{token}")
    public String formInvite(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("escuelas", escuelaRepo.findAll());
        model.addAttribute("form", new InviteCompleteForm());
        return "auth/invite-complete"; // templates/auth/invite-complete.html
    }

    @PostMapping("/invite/{token}")
    public String completarInvite(@PathVariable String token,
                                  @Valid @ModelAttribute("form") InviteCompleteForm form,
                                  BindingResult br, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("token", token);
            model.addAttribute("escuelas", escuelaRepo.findAll());
            return "auth/invite-complete";
        }
        try {
            usuarioService.completarRegistroConInvitacion(
                    token, form.getPassword(), form.getDni(),
                    form.getNombres(), form.getApellidos(), form.getEscuelaId());
        } catch (BizException ex) {
            model.addAttribute("token", token);
            model.addAttribute("escuelas", escuelaRepo.findAll());
            model.addAttribute("error", ex.getMessage());
            return "auth/invite-complete";
        }
        return "redirect:/login?registered";
    }
}