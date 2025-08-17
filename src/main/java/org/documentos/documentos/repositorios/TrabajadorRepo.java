package org.documentos.documentos.repositorios;

import org.documentos.documentos.entidades.Trabajador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TrabajadorRepo extends JpaRepository<Trabajador, Long> {
    Optional<Trabajador> findByDni(String dni);
    Optional<Trabajador> findByUsuarioId(Long usuarioId);
    @Query("""
           select t from Trabajador t
           join fetch t.usuario u
           join fetch t.escuela e
           """)
    Page<Trabajador> findAllWithUsuarioEscuela(Pageable pageable);
}