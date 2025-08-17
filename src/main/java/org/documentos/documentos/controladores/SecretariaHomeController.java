package org.documentos.documentos.controladores;

import lombok.RequiredArgsConstructor;
import org.documentos.documentos.entidades.enums.EstadoDocumento;
import org.documentos.documentos.repositorios.DocumentoRepo;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SecretariaHomeController {

    private final DocumentoRepo documentoRepo;
    private final TrabajadorRepo trabajadorRepo;

    @GetMapping("/secretaria")
    public String dashboard(Model model) {
        long docPendSecretaria = documentoRepo.countByEstado(EstadoDocumento.ENVIADO);   // llega a Secretaría
        long docPendJefa       = documentoRepo.countByEstado(EstadoDocumento.REV_JEFA);  // esperando Jefa
        long docAprobados      = documentoRepo.countByEstado(EstadoDocumento.APROBADO);
        long totalTrabajadores = trabajadorRepo.count();

        model.addAttribute("docPendSecretaria", docPendSecretaria);
        model.addAttribute("docPendJefa", docPendJefa);
        model.addAttribute("docAprobados", docAprobados);
        model.addAttribute("totalTrabajadores", totalTrabajadores);
        return "secretaria/dashboard";
    }
}