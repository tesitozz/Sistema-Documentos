package org.documentos.documentos.controladores;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SECRETARIA")))
            return "redirect:/secretaria/dashboard";
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_JEFA")))
            return "redirect:/jefa/revision";
        return "redirect:/trabajador/documentos";
    }
}