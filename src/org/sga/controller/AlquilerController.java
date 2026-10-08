package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private ComboBox<Vehiculo> cmbVehiculo;
    @FXML private Label lblDetalleVehiculo;
    @FXML private TextField txtCui;
    @FXML private Label lblEstadoCliente;
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

    // cliente encontrado por CUI (null si es un cliente nuevo)
    private Cliente clienteExistente;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vehiculoDAO = new VehiculoDAOImpl();
        alquilerDAO = new AlquilerDAOImpl();
        clienteDAO = new ClienteDAOImpl();
        lblMensaje.setText("");
        lblEstadoCliente.setText("");

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

        // si cambian el CUI, los datos del cliente anterior ya no aplican
        txtCui.textProperty().addListener((obs, anterior, nuevo) -> limpiarDatosCliente());

        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        lblAsesor.setText(actual != null ? actual.getUsername() : "");

        cargarVehiculos();
    }

    private void cargarVehiculos() {
        cmbVehiculo.setItems(FXCollections.observableArrayList(
                vehiculoDAO.buscarDisponibles("", "alquiler")));
    }

    @FXML
    public void eventoBuscarCliente(ActionEvent evento) {
        try {
            long cui = validarCui();
            Cliente cliente = clienteDAO.buscar(cui);
            if (cliente != null) {
                mostrarClienteExistente(cliente);
            } else {
                limpiarDatosCliente();
                lblEstadoCliente.setText("Cliente nuevo: completa sus datos");
            }
            lblMensaje.setText("");
        } catch (ValidarException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(cmbVehiculo.getValue(), "Selecciona el vehículo que se va a alquilar.");
            long cui = validarCui();
            Cliente cliente = obtenerClienteParaAlquiler(cui);
            LocalDate fechaRegreso = validarFechaRegreso();

            // el alquiler queda asociado al asesor que tiene la sesión activa
            Usuario actual = SessionContext.getInstancia().getUsuarioActual();
            ValidarException.validarNulo(actual, "No hay una sesión activa. Inicia sesión nuevamente.");

            Vehiculo vehiculo = cmbVehiculo.getValue();

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

            // si el cliente es nuevo, se registra antes del alquiler (la tabla alquileres lo requiere)
            if (clienteExistente == null && !clienteDAO.insertar(cliente)) {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar al cliente. Intenta nuevamente.");
                return;
            }

            Alquiler alquiler = new Alquiler();
            alquiler.setIdVehiculo(vehiculo.getId());
            alquiler.setCuiCliente(cliente.getCui());
            alquiler.setIdAsesor(actual.getId());
            alquiler.setFechaRegreso(fechaRegreso);
            alquiler.setLlevaSeguro(chkSeguro.isSelected());

            if (alquilerDAO.registrarAlquiler(alquiler)) {
                // las fechas las guardó la base de datos: aquí solo se consultan y se muestran
                Alquiler guardado = alquilerDAO.buscar(alquiler.getId());
                String detalle = "";
                if (guardado != null && guardado.getFechaRegistro() != null) {
                    detalle = " Registrado el " + guardado.getFechaRegistro().format(FORMATO_FECHA_HORA)
                            + ", devolución programada: " + guardado.getFechaRegreso().format(FORMATO_FECHA) + ".";
                }
                mostrarAlerta(Alert.AlertType.INFORMATION, "Alquiler del vehículo " + vehiculo.getPlaca()
                        + " registrado con éxito. El vehículo quedó como Alquilado." + detalle);
                limpiarCampos();
                cargarVehiculos();
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

    private long validarCui() throws ValidarException {
        ValidarException.validarNoVacio(txtCui.getText(), "CUI / DPI del cliente");
        String texto = txtCui.getText().trim();
        if (!texto.matches("\\d{13}")) {
            throw new ValidarException("El CUI debe tener exactamente 13 dígitos numéricos.");
        }
        return Long.parseLong(texto);
    }

    // T3.17: usa el cliente registrado o arma uno nuevo con DPI, nombre, apellido, teléfono y licencia
    private Cliente obtenerClienteParaAlquiler(long cui) throws ValidarException {
        Cliente existente = (clienteExistente != null) ? clienteExistente : clienteDAO.buscar(cui);
        if (existente != null) {
            mostrarClienteExistente(existente);
            return existente;
        }

        ValidarException.validarNoVacio(txtNombres.getText(), "nombres");
        ValidarException.validarNoVacio(txtApellidos.getText(), "apellidos");
        ValidarException.validarNoVacio(txtTelefono.getText(), "teléfono");
        ValidarException.validarNoVacio(txtLicencia.getText(), "licencia de conducir");

        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String licencia = txtLicencia.getText().trim();

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

        return new Cliente(cui, nombres, apellidos, telefono, null, licencia);
    }

    private LocalDate validarFechaRegreso() throws ValidarException {
        LocalDate fecha = dpFechaRegreso.getValue();
        ValidarException.validarNulo(fecha, "Selecciona la fecha programada de devolución.");
        if (!fecha.isAfter(LocalDate.now())) {
            throw new ValidarException("La fecha de devolución debe ser posterior a hoy.");
        }
        return fecha;
    }

    private void mostrarClienteExistente(Cliente cliente) {
        clienteExistente = cliente;
        txtNombres.setText(cliente.getNombres());
        txtApellidos.setText(cliente.getApellidos());
        txtTelefono.setText(cliente.getTelefono());
        txtLicencia.setText(cliente.getLicencia());
        establecerClienteEditable(false);
        lblEstadoCliente.setText("Cliente ya registrado");
    }

    private void limpiarDatosCliente() {
        clienteExistente = null;
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtLicencia.clear();
        establecerClienteEditable(true);
        lblEstadoCliente.setText("");
    }

    private void establecerClienteEditable(boolean editable) {
        txtNombres.setEditable(editable);
        txtApellidos.setEditable(editable);
        txtTelefono.setEditable(editable);
        txtLicencia.setEditable(editable);
    }

    private void limpiarCampos() {
        cmbVehiculo.setValue(null);
        lblDetalleVehiculo.setText("");
        txtCui.clear(); // el listener limpia los demás datos del cliente
        dpFechaRegreso.setValue(null);
        chkSeguro.setSelected(false);
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}