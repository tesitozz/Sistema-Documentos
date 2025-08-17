package org.documentos.documentos.servicios;

import org.documentos.documentos.entidades.*;
import org.documentos.documentos.entidades.enums.EstadoDocumento;
import org.documentos.documentos.entidades.enums.Rol;
import org.documentos.documentos.repositorios.DocumentoHistorialRepo;
import org.documentos.documentos.repositorios.DocumentoRepo;
import org.documentos.documentos.repositorios.TipoDocumentoRepo;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentoService {

    private final DocumentoRepo documentoRepo;
    private final DocumentoHistorialRepo historialRepo;
    private final TrabajadorRepo trabajadorRepo;
    private final TipoDocumentoRepo tipoRepo;
    private final StorageService storage;
    private final SecurityHelper security;

    public DocumentoService(DocumentoRepo documentoRepo,
                            DocumentoHistorialRepo historialRepo,
                            TrabajadorRepo trabajadorRepo,
                            TipoDocumentoRepo tipoRepo,
                            StorageService storage,
                            SecurityHelper security) {
        this.documentoRepo = documentoRepo;
        this.historialRepo = historialRepo;
        this.trabajadorRepo = trabajadorRepo;
        this.tipoRepo = tipoRepo;
        this.storage = storage;
        this.security = security;
    }

    // ---------- Consultas de bandeja ----------
    public Page<Documento> listarMisDocumentos(Pageable pageable) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.TRABAJADOR) throw new BizException("Solo trabajadores.");
        Trabajador t = trabajadorRepo.findByUsuarioId(u.getId())
                .orElseThrow(() -> new BizException("Trabajador no encontrado."));
        return documentoRepo.findByTrabajador(t, pageable);
    }

    public Page<Documento> bandejaSecretaria(Pageable pageable) {
        return documentoRepo.findByEstado(EstadoDocumento.ENVIADO, pageable);
    }

    public Page<Documento> bandejaJefa(Pageable pageable) {
        return documentoRepo.findByEstado(EstadoDocumento.REV_JEFA, pageable);
    }

    // ---------- Acciones del Trabajador ----------
    @Transactional
    public Documento crearBorrador(Long tipoId, String titulo, String resumen, MultipartFile pdf) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.TRABAJADOR) throw new BizException("Solo trabajadores.");
        Trabajador t = trabajadorRepo.findByUsuarioId(u.getId())
                .orElseThrow(() -> new BizException("Trabajador no encontrado."));
        TipoDocumento tipo = tipoRepo.findById(tipoId)
                .orElseThrow(() -> new BizException("Tipo de documento no existe."));

        String path = (pdf != null && !pdf.isEmpty()) ? storage.savePdf(pdf) : null;
        if (path == null) throw new BizException("PDF es requerido para crear.");

        Documento d = Documento.builder()
                .trabajador(t)
                .tipo(tipo)
                .titulo(titulo)
                .resumen(resumen)
                .archivoPath(path)
                .version(1)
                .estado(EstadoDocumento.BORRADOR)
                .responsableActual(null)
                .build();
        d = documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.BORRADOR, null);
        return d;
    }

    @Transactional
    public Documento enviar(Long documentoId) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.TRABAJADOR) throw new BizException("Solo trabajadores.");
        Documento d = documentoRepo.findById(documentoId)
                .orElseThrow(() -> new BizException("Documento no encontrado."));
        validarPropietario(u, d);
        if (d.getEstado() != EstadoDocumento.BORRADOR && d.getEstado() != EstadoDocumento.RECH_SECRETARIA && d.getEstado() != EstadoDocumento.RECH_JEFA)
            throw new BizException("Solo borradores o rechazados pueden enviarse.");

        d.setEstado(EstadoDocumento.ENVIADO);
        d.setResponsableActual("SECRETARIA");
        documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.ENVIADO, null);
        return d;
    }

    @Transactional
    public Documento resubirNuevaVersion(Long documentoId, MultipartFile nuevoPdf) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.TRABAJADOR) throw new BizException("Solo trabajadores.");
        Documento d = documentoRepo.findById(documentoId)
                .orElseThrow(() -> new BizException("Documento no encontrado."));
        validarPropietario(u, d);

        if (d.getEstado() != EstadoDocumento.RECH_SECRETARIA && d.getEstado() != EstadoDocumento.RECH_JEFA)
            throw new BizException("Solo documentos rechazados aceptan re-subida.");

        String path = storage.savePdf(nuevoPdf);
        d.setArchivoPath(path);
        d.setVersion(d.getVersion() + 1);
        d.setEstado(EstadoDocumento.ENVIADO);
        d.setResponsableActual("SECRETARIA");
        documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.ENVIADO, "Reenvío con nueva versión v" + d.getVersion());
        return d;
    }

    // ---------- Acciones de Secretaria ----------
    @Transactional
    public Documento secretariaRechazar(Long documentoId, String motivo) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.SECRETARIA) throw new BizException("Solo secretaria.");
        if (motivo == null || motivo.isBlank()) throw new BizException("Motivo es obligatorio.");

        Documento d = documentoRepo.findById(documentoId)
                .orElseThrow(() -> new BizException("Documento no encontrado."));
        if (d.getEstado() != EstadoDocumento.ENVIADO && d.getEstado() != EstadoDocumento.REV_SECRETARIA)
            throw new BizException("El documento no está en revisión de secretaria.");

        d.setEstado(EstadoDocumento.RECH_SECRETARIA);
        d.setResponsableActual(null);
        documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.RECH_SECRETARIA, motivo);
        return d;
    }

    @Transactional
    public Documento secretariaElevarAJefa(Long documentoId) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.SECRETARIA) throw new BizException("Solo secretaria.");

        Documento d = documentoRepo.findById(documentoId)
                .orElseThrow(() -> new BizException("Documento no encontrado."));
        if (d.getEstado() != EstadoDocumento.ENVIADO && d.getEstado() != EstadoDocumento.REV_SECRETARIA)
            throw new BizException("El documento no está listo para elevar.");

        d.setEstado(EstadoDocumento.REV_JEFA);
        d.setResponsableActual("JEFA");
        documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.REV_JEFA, "Elevado a jefa");
        return d;
    }

    // ---------- Acciones de Jefa ----------
    @Transactional
    public Documento jefaAprobar(Long documentoId) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.JEFA) throw new BizException("Solo jefa.");

        Documento d = documentoRepo.findById(documentoId)
                .orElseThrow(() -> new BizException("Documento no encontrado."));
        if (d.getEstado() != EstadoDocumento.REV_JEFA)
            throw new BizException("El documento no está en revisión de jefa.");

        d.setEstado(EstadoDocumento.APROBADO);
        d.setResponsableActual(null);
        documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.APROBADO, "Aprobado");
        return d;
    }

    @Transactional
    public Documento jefaRechazar(Long documentoId, String motivo) {
        Usuario u = security.currentUserOrThrow();
        if (u.getRol() != Rol.JEFA) throw new BizException("Solo jefa.");
        if (motivo == null || motivo.isBlank()) throw new BizException("Motivo es obligatorio.");

        Documento d = documentoRepo.findById(documentoId)
                .orElseThrow(() -> new BizException("Documento no encontrado."));
        if (d.getEstado() != EstadoDocumento.REV_JEFA)
            throw new BizException("El documento no está en revisión de jefa.");

        d.setEstado(EstadoDocumento.RECH_JEFA);
        d.setResponsableActual(null);
        documentoRepo.save(d);
        registrarHistorial(d, u, EstadoDocumento.RECH_JEFA, motivo);
        return d;
    }

    // ---------- Utilidades ----------
    private void validarPropietario(Usuario u, Documento d) {
        Trabajador t = trabajadorRepo.findByUsuarioId(u.getId())
                .orElseThrow(() -> new BizException("Trabajador no encontrado."));
        if (!d.getTrabajador().getId().equals(t.getId()))
            throw new BizException("No puedes operar documentos de otro trabajador.");
    }

    private void registrarHistorial(Documento d, Usuario actor, EstadoDocumento estado, String motivo) {
        DocumentoHistorial h = DocumentoHistorial.builder()
                .documento(d)
                .usuario(actor)
                .estado(estado)
                .motivo(motivo)
                .build();
        historialRepo.save(h);
    }
}