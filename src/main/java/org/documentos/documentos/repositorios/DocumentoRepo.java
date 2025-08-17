package org.documentos.documentos.repositorios;


import org.documentos.documentos.entidades.Documento;
import org.documentos.documentos.entidades.Trabajador;
import org.documentos.documentos.entidades.enums.EstadoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepo extends JpaRepository<Documento, Long> {
    Page<Documento> findByTrabajador(Trabajador trabajador, Pageable pageable);
    Page<Documento> findByEstado(EstadoDocumento estado, Pageable pageable);
    Page<Documento> findByEstadoAndTrabajador_Escuela_Id(EstadoDocumento estado, Long escuelaId, Pageable pageable);
    Page<Documento> findByTrabajador_DniContainingAndEstado(String dni, EstadoDocumento estado, Pageable pageable);

    long countByEstado(EstadoDocumento estado);
}