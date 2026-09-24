package org.sga.dao;

import org.sga.model.Usuario;

public interface UsuarioDAO extends CRUD<Usuario, Integer> {
    Usuario iniciarSesion(String username, String passwordHash);
    Usuario buscarPorUsername(String username);
    boolean desactivar(int id);
}
