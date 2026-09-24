package org.sga.manager;

import org.sga.dao.UsuarioDAO;
import org.sga.dao.impl.UsuarioDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.model.Usuario;
import org.sga.util.SecurityUtil;

public class AutenticacionService {

    public static final String MSG_CREDENCIALES_INCORRECTAS = "Usuario o contraseña incorrectos";
    public static final String MSG_USUARIO_INACTIVO = "El usuario está inactivo. Contacta al administrador";

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService() {
        this(new UsuarioDAOImpl());
    }

    public AutenticacionService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String username, String password) throws ValidarException {
        // T1.7: validar usuario y contraseña
        ValidarException.validarNoVacio(username, "usuario");
        ValidarException.validarNoVacio(password, "contraseña");

        String usernameLimpio = username.trim();
        String passwordHash = SecurityUtil.hashSHA256(password);

        // Camino feliz: el SP solo devuelve usuarios activos con credenciales correctas
        Usuario autenticado = usuarioDAO.iniciarSesion(usernameLimpio, passwordHash);
        if (autenticado != null) {
            return autenticado;
        }

        // T1.8: averiguar por qué falló
        Usuario existente = usuarioDAO.buscarPorUsername(usernameLimpio);
        if (existente != null && passwordHash.equals(existente.getPasswordHash()) && !existente.isActivo()) {
            throw new ValidarException(MSG_USUARIO_INACTIVO);
        }
        throw new ValidarException(MSG_CREDENCIALES_INCORRECTAS);
    }
}