package org.documentos.documentos.controladores;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.documentos.documentos.docsys.DocumentoForm;
import org.documentos.documentos.entidades.Documento;
import org.documentos.documentos.entidades.Trabajador;
import org.documentos.documentos.entidades.Usuario;
import org.documentos.documentos.entidades.enums.EstadoDocumento;
import org.documentos.documentos.repositorios.TipoDocumentoRepo;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.documentos.documentos.servicios.BizException;
import org.documentos.documentos.servicios.DocumentoService;
import org.documentos.documentos.servicios.SecurityHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/trabajador")
@RequiredArgsConstructor
public class TrabajadorController {

    private final DocumentoService documentoService;
    private final TipoDocumentoRepo tipoRepo;
    private final SecurityHelper securityHelper;
    private final TrabajadorRepo trabajadorRepo;

    @GetMapping("/documentos")
    public String misDocumentos(@PageableDefault(size=10) Pageable pageable, Model model) {
        // Obtener el trabajador actual
        Usuario usuarioActual = securityHelper.currentUserOrThrow();
        Trabajador trabajadorActual = trabajadorRepo.findByUsuarioId(usuarioActual.getId())
                .orElseThrow(() -> new BizException("Trabajador no encontrado."));

        // Obtener los documentos
        Page<Documento> page = documentoService.listarMisDocumentos(pageable);

        // Calcular estadísticas (opcional)
        Map<String, Long> estadisticas = calcularEstadisticas(page, trabajadorActual);

        // Agregar datos al modelo
        model.addAttribute("page", page);
        model.addAttribute("trabajadorActual", trabajadorActual);
        model.addAttribute("estadisticas", estadisticas);

        return "trabajador/documentos"; // templates/trabajador/documentos.html
    }

    @GetMapping("/documentos/nuevo")
    public String nuevo(Model model) {
        // Obtener el trabajador actual para la vista
        Usuario usuarioActual = securityHelper.currentUserOrThrow();
        Trabajador trabajadorActual = trabajadorRepo.findByUsuarioId(usuarioActual.getId())
                .orElseThrow(() -> new BizException("Trabajador no encontrado."));

        model.addAttribute("doc", new DocumentoForm());
        model.addAttribute("tipos", tipoRepo.findAll());
        model.addAttribute("trabajadorActual", trabajadorActual);

        return "trabajador/documento-form"; // formulario
    }

    @PostMapping("/documentos")
    public String crear(@Valid @ModelAttribute("doc") DocumentoForm form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            // Re-obtener datos para la vista en caso de error
            Usuario usuarioActual = securityHelper.currentUserOrThrow();
            Trabajador trabajadorActual = trabajadorRepo.findByUsuarioId(usuarioActual.getId())
                    .orElseThrow(() -> new BizException("Trabajador no encontrado."));

            model.addAttribute("tipos", tipoRepo.findAll());
            model.addAttribute("trabajadorActual", trabajadorActual);
            return "trabajador/documento-form";
        }
        try {
            Documento d = documentoService.crearBorrador(form.getTipoId(), form.getTitulo(), form.getResumen(), form.getArchivo());
            ra.addFlashAttribute("ok", "Borrador creado exitosamente. Ahora puedes enviarlo para revisión.");
            return "redirect:/trabajador/documentos";
        } catch (BizException ex) {
            // Re-obtener datos para la vista en caso de error
            Usuario usuarioActual = securityHelper.currentUserOrThrow();
            Trabajador trabajadorActual = trabajadorRepo.findByUsuarioId(usuarioActual.getId())
                    .orElseThrow(() -> new BizException("Trabajador no encontrado."));

            model.addAttribute("tipos", tipoRepo.findAll());
            model.addAttribute("trabajadorActual", trabajadorActual);
            model.addAttribute("error", ex.getMessage());
            return "trabajador/documento-form";
        }
    }

    @PostMapping("/documentos/{id}/enviar")
    public String enviar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            documentoService.enviar(id);
            ra.addFlashAttribute("ok", "Documento enviado a Secretaría para revisión.");
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
            ra.addFlashAttribute("ok", "Nueva versión subida y enviada correctamente.");
        } catch (BizException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/trabajador/documentos";
    }

    /**
     * Calcula estadísticas básicas de los documentos del trabajador
     */
    private Map<String, Long> calcularEstadisticas(Page<Documento> page, Trabajador trabajador) {
        Map<String, Long> stats = new HashMap<>();

        // Contar documentos por estado desde todos los documentos del trabajador (no solo la página actual)
        // En una implementación más eficiente, esto se haría con queries específicas
        long total = page.getTotalElements();
        long borradores = page.getContent().stream()
                .mapToLong(d -> d.getEstado() == EstadoDocumento.BORRADOR ? 1 : 0)
                .sum();
        long pendientes = page.getContent().stream()
                .mapToLong(d -> (d.getEstado() == EstadoDocumento.ENVIADO ||
                        d.getEstado() == EstadoDocumento.REV_JEFA) ? 1 : 0)
                .sum();
        long aprobados = page.getContent().stream()
                .mapToLong(d -> d.getEstado() == EstadoDocumento.APROBADO ? 1 : 0)
                .sum();

        stats.put("total", total);
        stats.put("borradores", borradores);
        stats.put("pendientes", pendientes);
        stats.put("aprobados", aprobados);

        return stats;
    }
}