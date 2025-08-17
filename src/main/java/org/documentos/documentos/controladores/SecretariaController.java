package org.documentos.documentos.controladores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.documentos.documentos.docsys.MotivoForm;
import org.documentos.documentos.servicios.BizException;
import org.documentos.documentos.servicios.DocumentoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/secretaria")
@RequiredArgsConstructor
public class SecretariaController {

    private final DocumentoService documentoService;

    // Endpoint para el dashboard principal de la secretaria
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Aquí puedes agregar estadísticas o información del dashboard
        // Por ejemplo: cantidad de documentos pendientes, resumen, etc.

        // Ejemplo de datos que podrías mostrar en el dashboard:
        // long documentosPendientes = documentoService.contarDocumentosPendientesSecretaria();
        // model.addAttribute("documentosPendientes", documentosPendientes);

        return "secretaria/dashboard"; // Vista del dashboard
    }

    @GetMapping("/revision")
    public String bandeja(@PageableDefault(size=10) Pageable pageable, Model model) {
        model.addAttribute("page", documentoService.bandejaSecretaria(pageable));
        model.addAttribute("motivo", new MotivoForm());
        return "secretaria/revision"; // lista con botones elevar/rechazar
    }

    @PostMapping("/documentos/{id}/elevar")
    public String elevar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            documentoService.secretariaElevarAJefa(id);
            ra.addFlashAttribute("ok", "Documento elevado a Jefa.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/secretaria/revision";
    }

    @PostMapping("/documentos/{id}/rechazar")
    public String rechazar(@PathVariable Long id,
                           @Valid @ModelAttribute("motivo") MotivoForm motivo,
                           BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Motivo es obligatorio.");
            return "redirect:/secretaria/revision";
        }
        try {
            documentoService.secretariaRechazar(id, motivo.getMotivo());
            ra.addFlashAttribute("ok", "Documento rechazado.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/secretaria/revision";
    }
}