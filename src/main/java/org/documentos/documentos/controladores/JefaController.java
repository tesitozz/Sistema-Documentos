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
@RequestMapping("/jefa")
@RequiredArgsConstructor
public class JefaController {

    private final DocumentoService documentoService;

    @GetMapping("/revision")
    public String bandeja(@PageableDefault(size=10) Pageable pageable, Model model) {
        model.addAttribute("page", documentoService.bandejaJefa(pageable));
        model.addAttribute("motivo", new MotivoForm());
        return "jefa/revision"; // lista con aprobar/rechazar
    }

    @PostMapping("/documentos/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            documentoService.jefaAprobar(id);
            ra.addFlashAttribute("ok", "Documento aprobado.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/jefa/revision";
    }

    @PostMapping("/documentos/{id}/rechazar")
    public String rechazar(@PathVariable Long id,
                           @Valid @ModelAttribute("motivo") MotivoForm motivo,
                           BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Motivo es obligatorio.");
            return "redirect:/jefa/revision";
        }
        try {
            documentoService.jefaRechazar(id, motivo.getMotivo());
            ra.addFlashAttribute("ok", "Documento rechazado.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/jefa/revision";
    }
}
