package org.documentos.documentos.controladores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.documentos.documentos.docsys.DocumentoForm;
import org.documentos.documentos.entidades.Documento;
import org.documentos.documentos.repositorios.TipoDocumentoRepo;
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
@RequestMapping("/trabajador")
@RequiredArgsConstructor
public class TrabajadorController {

    private final DocumentoService documentoService;
    private final TipoDocumentoRepo tipoRepo;

    @GetMapping("/documentos")
    public String misDocumentos(@PageableDefault(size=10) Pageable pageable, Model model) {
        model.addAttribute("page", documentoService.listarMisDocumentos(pageable));
        return "trabajador/documentos"; // lista
    }

    @GetMapping("/documentos/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("doc", new DocumentoForm());
        model.addAttribute("tipos", tipoRepo.findAll());
        return "trabajador/documento-form"; // formulario
    }

    @PostMapping("/documentos")
    public String crear(@Valid @ModelAttribute("doc") DocumentoForm form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("tipos", tipoRepo.findAll());
            return "trabajador/documento-form";
        }
        try {
            Documento d = documentoService.crearBorrador(form.getTipoId(), form.getTitulo(), form.getResumen(), form.getArchivo());
            // En MVP lo dejamos en BORRADOR. Si quieres enviar directo, llama documentoService.enviar(d.getId()).
            ra.addFlashAttribute("ok", "Borrador creado. Ahora puedes enviarlo.");
            return "redirect:/trabajador/documentos";
        } catch (BizException ex) {
            model.addAttribute("tipos", tipoRepo.findAll());
            model.addAttribute("error", ex.getMessage());
            return "trabajador/documento-form";
        }
    }

    @PostMapping("/documentos/{id}/enviar")
    public String enviar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            documentoService.enviar(id);
            ra.addFlashAttribute("ok", "Documento enviado a Secretaría.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/trabajador/documentos";
    }

    @PostMapping("/documentos/{id}/resubir")
    public String resubir(@PathVariable Long id,
                          @RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo,
                          RedirectAttributes ra) {
        try {
            documentoService.resubirNuevaVersion(id, archivo);
            ra.addFlashAttribute("ok", "Nueva versión subida y enviada.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/trabajador/documentos";
    }
}