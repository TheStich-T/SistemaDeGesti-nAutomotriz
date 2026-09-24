package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.sga.dao.UsuarioDAO;
import org.sga.dao.impl.UsuarioDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.system.Main;
import org.sga.util.SecurityUtil;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnIniciarSesion;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        lblMensaje.setText("");
    }

    @FXML
    public void eventoIniciarSesion(ActionEvent evento) {
        lblMensaje.setText("");
        try {
            // T1.7: validar usuario y contraseña
            ValidarException.validarNoVacio(txtUsuario.getText(), "usuario");
            ValidarException.validarNoVacio(txtPassword.getText(), "contraseña");

            String username = txtUsuario.getText().trim();
            String passwordHash = SecurityUtil.hashSHA256(txtPassword.getText());

            Usuario usuarioEncontrado = usuarioDAO.buscarPorUsername(username);

            // T1.8: usuario inexistente o contraseña incorrecta
            if (usuarioEncontrado == null
                    || !passwordHash.equals(usuarioEncontrado.getPasswordHash())) {
                lblMensaje.setText("Usuario o contraseña incorrectos");
                txtPassword.clear();
                return;
            }

            // T1.8: usuario inactivo (solo se revela si la contraseña era correcta)
            if (!usuarioEncontrado.isActivo()) {
                lblMensaje.setText("El usuario está inactivo. Contacta al administrador");
                txtPassword.clear();
                return;
            }

            // No se guarda el hash en la sesión
            usuarioEncontrado.setPasswordHash(null);

            String dashboard = RolPermisos.getDashboardPorRol(usuarioEncontrado.getRol());
            if (dashboard == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Rol no reconocido: " + usuarioEncontrado.getRol());
                return;
            }

            SessionContext.getInstancia().setUsuarioActual(usuarioEncontrado);
            Main.cambiarEscena(dashboard);

        } catch (ValidarException e) {
            lblMensaje.setText(e.getMessage());
        } catch (IOException e) {
            SessionContext.getInstancia().cerrarSesion();
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}