package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.sga.dao.AlquilerDAO;
import org.sga.dao.ClienteDAO;
import org.sga.dao.VehiculoDAO;
import org.sga.dao.impl.AlquilerDAOImpl;
import org.sga.dao.impl.ClienteDAOImpl;
import org.sga.dao.impl.VehiculoDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Alquiler;
import org.sga.model.Cliente;
import org.sga.model.Usuario;
import org.sga.model.Vehiculo;
import org.sga.system.Main;

public class AlquilerController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private ComboBox<Vehiculo> cmbVehiculo;
    @FXML private Label lblDetalleVehiculo;
    @FXML private TextField txtCui;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtLicencia;
    @FXML private DatePicker dpFechaRegreso;
    @FXML private CheckBox chkSeguro;
    @FXML private Label lblAsesor;
    @FXML private Label lblMensaje;

    private VehiculoDAO vehiculoDAO;
    private AlquilerDAO alquilerDAO;
    private ClienteDAO clienteDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vehiculoDAO = new VehiculoDAOImpl();
        alquilerDAO = new AlquilerDAOImpl();
        clienteDAO = new ClienteDAOImpl();
        lblMensaje.setText("");

        cmbVehiculo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehiculo vehiculo) {
                if (vehiculo == null) {
                    return "";
                }
                return vehiculo.getPlaca() + " - " + vehiculo.getMarca() + " "
                        + vehiculo.getModelo() + " " + vehiculo.getAnio();
            }

            @Override
            public Vehiculo fromString(String texto) {
                return null;
            }
        });

        cmbVehiculo.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                lblDetalleVehiculo.setText("Condición: " + seleccionado.getCondicion()
                        + "  |  Tipo: " + seleccionado.getTipo()
                        + "  |  Color: " + seleccionado.getColor());
                lblMensaje.setText("");
            }
        });

        // T3.19: solo se puede programar la devolución a partir de mañana
        dpFechaRegreso.setEditable(false);
        dpFechaRegreso.setDayCellFactory(selector -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                setDisable(vacio || !fecha.isAfter(LocalDate.now()));
            }
        });

        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        lblAsesor.setText(actual != null ? actual.getUsername() : "");

        cargarVehiculos();
    }

    private void cargarVehiculos() {
        cmbVehiculo.setItems(FXCollections.observableArrayList(
                vehiculoDAO.buscarDisponibles("", "alquiler")));
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(cmbVehiculo.getValue(), "Selecciona el vehículo que se va a alquilar.");
            Cliente cliente = validarCliente();
            LocalDate fechaRegreso = validarFechaRegreso();

            // el alquiler queda asociado al asesor que tiene la sesión activa
            Usuario actual = SessionContext.getInstancia().getUsuarioActual();
            ValidarException.validarNulo(actual, "No hay una sesión activa. Inicia sesión nuevamente.");

            Vehiculo vehiculo = cmbVehiculo.getValue();
            boolean llevaSeguro = chkSeguro.isSelected();

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Registrar el alquiler del vehículo " + vehiculo.getPlaca() + " a "
                    + cliente.getNombreCompleto() + " con devolución el "
                    + fechaRegreso.format(FORMATO_FECHA) + "?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirmacion.setHeaderText("Confirmar alquiler");
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
                return;
            }

            // primero se guarda el cliente (si el CUI ya existe, se actualizan sus datos)
            if (!clienteDAO.guardar(cliente)) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron guardar los datos del cliente. "
                        + "Intenta nuevamente.");
                return;
            }

            Alquiler alquiler = new Alquiler();
            alquiler.setIdVehiculo(vehiculo.getId());
            alquiler.setCuiCliente(cliente.getCui());
            alquiler.setIdAsesor(actual.getId());
            alquiler.setFechaRegreso(fechaRegreso);
            alquiler.setLlevaSeguro(llevaSeguro);

            if (alquilerDAO.registrarAlquiler(alquiler)) {
                // las fechas las guardó la base de datos: aquí solo se consultan y se muestran
                Alquiler guardado = alquilerDAO.buscar(alquiler.getId());
                LocalDateTime fechaRegistro = guardado != null ? guardado.getFechaRegistro() : null;
                LocalDate fechaSalida = guardado != null ? guardado.getFechaSalida() : null;
                String descripcion = vehiculo.getMarca() + " " + vehiculo.getModelo() + " " + vehiculo.getAnio();

                limpiarCampos();
                cargarVehiculos();

                try {
                    TicketController.mostrar(alquiler.getId(), fechaRegistro, cliente.getNombreCompleto(),
                            cliente.getCui(), vehiculo.getPlaca(), descripcion, fechaSalida, fechaRegreso,
                            llevaSeguro, actual.getUsername());
                } catch (IOException e) {
                    mostrarAlerta(Alert.AlertType.WARNING, "El alquiler se registró, pero no se pudo abrir "
                            + "el ticket: " + e.getMessage());
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar el alquiler. "
                        + "Verifica que el vehículo siga Disponible.");
                cargarVehiculos();
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

    // arma el cliente con los datos del formulario
    private Cliente validarCliente() throws ValidarException {
        ValidarException.validarNoVacio(txtCui.getText(), "CUI / DPI del cliente");
        ValidarException.validarNoVacio(txtNombres.getText(), "nombres");
        ValidarException.validarNoVacio(txtApellidos.getText(), "apellidos");
        ValidarException.validarNoVacio(txtTelefono.getText(), "teléfono");
        ValidarException.validarNoVacio(txtLicencia.getText(), "licencia de conducir");

        String cui = txtCui.getText().trim();
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String licencia = txtLicencia.getText().trim();

        if (!cui.matches("\\d{13}")) {
            throw new ValidarException("El CUI debe tener exactamente 13 dígitos numéricos.");
        }
        if (!nombres.matches("[\\p{L} .'-]{1,100}")) {
            throw new ValidarException("Los nombres solo pueden contener letras (máximo 100 caracteres).");
        }
        if (!apellidos.matches("[\\p{L} .'-]{1,100}")) {
            throw new ValidarException("Los apellidos solo pueden contener letras (máximo 100 caracteres).");
        }
        if (!telefono.matches("\\d{8,15}")) {
            throw new ValidarException("El teléfono debe tener entre 8 y 15 dígitos numéricos.");
        }
        if (licencia.length() > 30) {
            throw new ValidarException("La licencia admite máximo 30 caracteres.");
        }

        return new Cliente(Long.parseLong(cui), nombres, apellidos, telefono, null, licencia);
    }

    private LocalDate validarFechaRegreso() throws ValidarException {
        LocalDate fecha = dpFechaRegreso.getValue();
        ValidarException.validarNulo(fecha, "Selecciona la fecha programada de devolución.");
        if (!fecha.isAfter(LocalDate.now())) {
            throw new ValidarException("La fecha de devolución debe ser posterior a hoy.");
        }
        return fecha;
    }

    private void limpiarCampos() {
        cmbVehiculo.setValue(null);
        lblDetalleVehiculo.setText("");
        txtCui.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtLicencia.clear();
        dpFechaRegreso.setValue(null);
        chkSeguro.setSelected(false);
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}