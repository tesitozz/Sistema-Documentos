package org.documentos.documentos.servicios;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.documentos.documentos.entidades.Trabajador;
import org.documentos.documentos.repositorios.TrabajadorRepo;
import org.documentos.documentos.repositorios.UsuarioRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GestionTrabajadorService {

    private final TrabajadorRepo trabajadorRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Desactiva el acceso (soft delete): no borra filas, mantiene historial.
     */
    @Transactional
    public void desactivarTrabajador(Long trabajadorId) {
        Trabajador t = trabajadorRepo.findById(trabajadorId)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado"));
        t.getUsuario().setActivo(false);
        trabajadorRepo.save(t);
    }

    /**
     * Reactiva al trabajador (opcional).
     */
    @Transactional
    public void reactivarTrabajador(Long trabajadorId) {
        Trabajador t = trabajadorRepo.findById(trabajadorId)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado"));
        t.getUsuario().setActivo(true);
        trabajadorRepo.save(t);
    }
}