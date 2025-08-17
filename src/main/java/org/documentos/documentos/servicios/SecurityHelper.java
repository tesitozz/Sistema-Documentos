package org.documentos.documentos.servicios;

import org.documentos.documentos.entidades.Usuario;
import org.documentos.documentos.repositorios.UsuarioRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {

    private final UsuarioRepo usuarioRepo;

    public SecurityHelper(UsuarioRepo usuarioRepo) { this.usuarioRepo = usuarioRepo; }

    public Usuario currentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new BizException("No autenticado.");
        return usuarioRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new BizException("Usuario no encontrado."));
    }
}
