package org.documentos.documentos.repositorios;

import org.documentos.documentos.entidades.Escuela;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EscuelaRepo extends JpaRepository<Escuela, Long> {
    Optional<Escuela> findByNombre(String nombre);
}
