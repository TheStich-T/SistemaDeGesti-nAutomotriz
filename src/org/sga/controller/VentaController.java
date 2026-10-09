package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.sga.dao.ClienteDAO;
import org.sga.dao.VehiculoDAO;
import org.sga.dao.VentaDAO;
import org.sga.dao.impl.ClienteDAOImpl;
import org.sga.dao.impl.VehiculoDAOImpl;
import org.sga.dao.impl.VentaDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Cliente;
import org.sga.model.Usuario;
import org.sga.model.Vehiculo;
import org.sga.model.Venta;
import org.sga.system.Main;

public class VentaController implements Initializable {

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    @FXML private ComboBox<Vehiculo> cmbVehiculo;
    @FXML private Label lblDetalleVehiculo;
    @FXML private TextField txtCui;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtLicencia;
    @FXML private TextField txtPrecio;
    @FXML private Label lblAsesor;
    @FXML private Label lblMensaje;

    private VehiculoDAO vehiculoDAO;
    private VentaDAO ventaDAO;
    private ClienteDAO clienteDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vehiculoDAO = new VehiculoDAOImpl();
        ventaDAO = new VentaDAOImpl();
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
                txtPrecio.setText(seleccionado.getCosto().toPlainString());
                lblDetalleVehiculo.setText("Condición: " + seleccionado.getCondicion()
                        + "  |  Precio registrado: " + String.format("Q %,.2f", seleccionado.getCosto()));
                lblMensaje.setText("");
            }
        });

        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        lblAsesor.setText(actual != null ? actual.getUsername() : "");

        cargarVehiculos();
    }

    private void cargarVehiculos() {
        cmbVehiculo.setItems(FXCollections.observableArrayList(
                vehiculoDAO.buscarDisponibles("", "venta")));
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(cmbVehiculo.getValue(), "Selecciona el vehículo que se va a vender.");
            Cliente cliente = validarCliente();
            ValidarException.validarNoVacio(txtPrecio.getText(), "precio de venta");
            BigDecimal precio = validarPrecio(txtPrecio.getText().trim());

            // la venta queda asociada al asesor que tiene la sesión activa
            Usuario actual = SessionContext.getInstancia().getUsuarioActual();
            ValidarException.validarNulo(actual, "No hay una sesión activa. Inicia sesión nuevamente.");

            Vehiculo vehiculo = cmbVehiculo.getValue();

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Registrar la venta del vehículo " + vehiculo.getPlaca() + " a "
                    + cliente.getNombreCompleto() + " por " + String.format("Q %,.2f", precio) + "?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirmacion.setHeaderText("Confirmar venta");
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

            Venta venta = new Venta();
            venta.setIdVehiculo(vehiculo.getId());
            venta.setCuiCliente(cliente.getCui());
            venta.setIdAsesor(actual.getId());
            venta.setPrecio(precio);

            if (ventaDAO.registrarVenta(venta)) {
                // la fecha la guardó la base de datos: aquí solo se consulta y se muestra
                Venta guardada = ventaDAO.buscar(venta.getId());
                LocalDateTime fecha = guardada != null ? guardada.getFechaVenta() : null;
                String descripcion = vehiculo.getMarca() + " " + vehiculo.getModelo() + " " + vehiculo.getAnio();

                limpiarCampos();
                cargarVehiculos();

                try {
                    FacturaController.mostrar(venta.getId(), fecha, cliente.getNombreCompleto(),
                            cliente.getCui(), vehiculo.getPlaca(), descripcion, precio, actual.getUsername());
                } catch (IOException e) {
                    mostrarAlerta(Alert.AlertType.WARNING, "La venta se registró, pero no se pudo abrir "
                            + "la factura: " + e.getMessage());
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar la venta. "
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
    
    @FXML
    public void eventoBuscarCliente(ActionEvent evento) {
        String cui = txtCui.getText() == null ? "" : txtCui.getText().trim();
        if (!cui.matches("\\d{13}")) {
            mostrarAlerta(Alert.AlertType.WARNING, "El CUI debe tener exactamente 13 dígitos numéricos.");
            return;
        }
        Cliente cliente = clienteDAO.buscar(Long.parseLong(cui));
        if (cliente == null) {
            return; // cliente nuevo: se llenan los datos a mano
        }
        txtNombres.setText(cliente.getNombres());
        txtApellidos.setText(cliente.getApellidos());
        txtTelefono.setText(cliente.getTelefono());
        txtLicencia.setText(cliente.getLicencia());
    }

    // arma el cliente con los datos del formulario
    private Cliente validarCliente() throws ValidarException {
        ValidarException.validarNoVacio(txtCui.getText(), "CUI del cliente");
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

    private BigDecimal validarPrecio(String texto) throws ValidarException {
        BigDecimal precio;
        try {
            precio = new BigDecimal(texto.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new ValidarException("El precio de venta debe ser un número válido.");
        }
        if (precio.signum() <= 0) {
            throw new ValidarException("El precio de venta debe ser mayor que cero.");
        }
        if (precio.scale() > 2) {
            throw new ValidarException("El precio de venta admite máximo 2 decimales.");
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new ValidarException("El precio de venta excede el máximo permitido.");
        }
        return precio;
    }

    private void limpiarCampos() {
        cmbVehiculo.setValue(null);
        lblDetalleVehiculo.setText("");
        txtCui.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtLicencia.clear();
        txtPrecio.clear();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}