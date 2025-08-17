package org.documentos.documentos.repositorios;

import org.documentos.documentos.entidades.Documento;
import org.documentos.documentos.entidades.DocumentoHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoHistorialRepo extends JpaRepository<DocumentoHistorial, Long> {
    List<DocumentoHistorial> findByDocumentoOrderByFechaAsc(Documento documento);
}
