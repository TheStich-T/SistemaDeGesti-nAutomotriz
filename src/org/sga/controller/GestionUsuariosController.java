package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.sga.dao.UsuarioDAO;
import org.sga.dao.impl.UsuarioDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.system.Main;
import org.sga.util.SecurityUtil;

public class GestionUsuariosController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colActivo;
    @FXML private TableColumn<Usuario, String> colFechaCreacion;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Label lblMensaje;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO = new UsuarioDAOImpl();
        cmbRol.setItems(FXCollections.observableArrayList("admin", "provisionador", "mecanico", "asesor"));
        lblMensaje.setText("");

        colUsername.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getUsername()));
        colRol.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getRol()));
        colActivo.setCellValueFactory(dato
                -> new SimpleStringProperty(dato.getValue().isActivo() ? "Activo" : "Inactivo"));
        colFechaCreacion.setCellValueFactory(dato -> {
            LocalDateTime fecha = dato.getValue().getFechaCreacion();
            return new SimpleStringProperty(fecha != null ? fecha.format(FORMATO_FECHA) : "");
        });

        cargarUsuarios();

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtUsername.setText(seleccionado.getUsername());
                txtUsername.setDisable(true); // el username no se edita una vez creado
                cmbRol.setValue(seleccionado.getRol());
                txtPassword.clear();
                txtPassword.setDisable(true); // la contraseña no se cambia desde aquí
                lblMensaje.setText("");
            }
        });
    }

    private void cargarUsuarios() {
        tblUsuarios.setItems(FXCollections.observableArrayList(usuarioDAO.listar()));
    }

    @FXML
    public void eventoAgregar(ActionEvent evento) {
        try {
            // --- Validaciones ---
            ValidarException.validarNoVacio(txtUsername.getText(), "usuario");
            ValidarException.validarNoVacio(txtPassword.getText(), "contraseña");
            ValidarException.validarNulo(cmbRol.getValue(), "Debe seleccionar un rol.");

            String username = txtUsername.getText().trim();

            // Sin espacios en el username
            if (username.matches(".*\\s.*")) {
                throw new ValidarException("El nombre de usuario no puede contener espacios.");
            }

            // La columna username es varchar(50)
            if (username.length() > 50) {
                throw new ValidarException("El nombre de usuario no puede tener más de 50 caracteres.");
            }

            // Mínimo 8 caracteres en contraseña
            ValidarException.validarLongitudMinima(txtPassword.getText(), 8,
                    "La contraseña debe tener al menos 8 caracteres.");

            // Username repetido
            if (usuarioDAO.buscarPorUsername(username) != null) {
                throw new ValidarException("El nombre de usuario ya existe.");
            }

            // --- Construir y guardar ---
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setPasswordHash(SecurityUtil.hashSHA256(txtPassword.getText()));
            nuevoUsuario.setRol(cmbRol.getValue());

            if (usuarioDAO.insertar(nuevoUsuario)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario creado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo crear el usuario.");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla.");
            ValidarException.validarNulo(cmbRol.getValue(), "Debe seleccionar un rol.");

            // El admin no puede quitarse su propio rol (se quedaría sin acceso de administrador)
            if (esUsuarioActual(seleccionado) && !cmbRol.getValue().equals(seleccionado.getRol())) {
                throw new ValidarException("No puedes cambiar tu propio rol.");
            }

            Usuario modificado = copiar(seleccionado, cmbRol.getValue(), seleccionado.isActivo());

            if (usuarioDAO.actualizar(modificado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario actualizado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el usuario.");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoDesactivar(ActionEvent evento) {
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla.");

            // El admin no se puede desactivar a sí mismo
            if (esUsuarioActual(seleccionado)) {
                throw new ValidarException("No puedes desactivar tu propia cuenta.");
            }
            if (!seleccionado.isActivo()) {
                throw new ValidarException("El usuario ya está inactivo.");
            }

            if (usuarioDAO.desactivar(seleccionado.getId())) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario desactivado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo desactivar el usuario.");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoActivar(ActionEvent evento) {
        try {
            Usuario seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
            ValidarException.validarNulo(seleccionado, "Selecciona un usuario de la tabla.");

            if (seleccionado.isActivo()) {
                throw new ValidarException("El usuario ya está activo.");
            }

            // No hay método activar en el DAO: se usa actualizar con activo = true
            Usuario activado = copiar(seleccionado, seleccionado.getRol(), true);

            if (usuarioDAO.actualizar(activado)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Usuario activado con éxito.");
                limpiarCampos();
                cargarUsuarios();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo activar el usuario.");
            }

        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoLimpiar(ActionEvent evento) {
        limpiarCampos();
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            Usuario actual = SessionContext.getInstancia().getUsuarioActual();
            String dashboard = (actual != null) ? RolPermisos.getDashboardPorRol(actual.getRol()) : null;
            Main.cambiarEscena(dashboard != null ? dashboard : "/org/sga/view/LoginView.fxml");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        }
    }

    // Copia solo lo que necesita sp_actualizarusuario, para no modificar el objeto de la tabla
    private Usuario copiar(Usuario base, String rol, boolean activo) {
        Usuario copia = new Usuario();
        copia.setId(base.getId());
        copia.setUsername(base.getUsername());
        copia.setRol(rol);
        copia.setActivo(activo);
        return copia;
    }

    private boolean esUsuarioActual(Usuario usuario) {
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        return actual != null && actual.getId() == usuario.getId();
    }

    private void limpiarCampos() {
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtPassword.clear();
        txtPassword.setDisable(false);
        cmbRol.setValue(null);
        lblMensaje.setText("");
        tblUsuarios.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}
