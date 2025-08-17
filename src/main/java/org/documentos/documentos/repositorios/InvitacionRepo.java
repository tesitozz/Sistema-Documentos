package org.documentos.documentos.repositorios;

import org.documentos.documentos.entidades.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitacionRepo extends JpaRepository<Invitacion, Long> {
    Optional<Invitacion> findByToken(String token);
    Optional<Invitacion> findByEmail(String email);
}
