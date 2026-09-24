package org.sga.manager;

import org.sga.model.Usuario;

public class SessionContext {

    private static SessionContext instancia;
    private Usuario usuarioActual;

    private SessionContext() {
    }

    public static synchronized SessionContext getInstancia() {
        if (instancia == null) {
            instancia = new SessionContext();
        }
        return instancia;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }
}