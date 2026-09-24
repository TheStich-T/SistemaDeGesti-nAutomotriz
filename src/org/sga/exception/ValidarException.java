package org.sga.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ValidarException extends Exception {

    private static final Logger log = Logger.getLogger(ValidarException.class.getName());

    public ValidarException(String mensaje) {
        super(mensaje);
    }

    public static void validarNoVacio(String valor, String nombreCampo) throws ValidarException {
        if (valor == null || valor.trim().isEmpty()) {
            log.log(Level.WARNING, "Validación fallida: el campo {0} está vacío", nombreCampo);
            throw new ValidarException("El campo " + nombreCampo + " no puede estar vacío");
        }
    }
}