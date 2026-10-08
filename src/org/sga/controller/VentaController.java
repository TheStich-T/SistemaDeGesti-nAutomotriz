package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
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
    @FXML private Label lblCliente;
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
    public void eventoBuscarCliente(ActionEvent evento) {
        try {
            Cliente cliente = obtenerCliente();
            lblCliente.setText(cliente.getNombreCompleto());
            lblMensaje.setText("");
        } catch (ValidarException e) {
            lblCliente.setText("");
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
            ValidarException.validarNulo(cmbVehiculo.getValue(), "Selecciona el vehículo que se va a vender.");
            Cliente cliente = obtenerCliente();
            ValidarException.validarNoVacio(txtPrecio.getText(), "precio de venta");
            BigDecimal precio = validarPrecio(txtPrecio.getText().trim());

            // T3.8: la venta queda asociada al asesor que tiene la sesión activa
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

            Venta venta = new Venta();
            venta.setIdVehiculo(vehiculo.getId());
            venta.setCuiCliente(cliente.getCui());
            venta.setIdAsesor(actual.getId());
            venta.setPrecio(precio);

           
            if (ventaDAO.registrarVenta(venta)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Venta del vehículo " + vehiculo.getPlaca()
                        + " registrada con éxito. El vehículo quedó como Vendido.");
                limpiarCampos();
                cargarVehiculos();
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

    private Cliente obtenerCliente() throws ValidarException {
        ValidarException.validarNoVacio(txtCui.getText(), "CUI del cliente");
        String texto = txtCui.getText().trim();
        if (!texto.matches("\\d{13}")) {
            throw new ValidarException("El CUI debe tener exactamente 13 dígitos numéricos.");
        }
        Cliente cliente = clienteDAO.buscar(Long.parseLong(texto));
        ValidarException.validarNulo(cliente, "No existe un cliente registrado con el CUI " + texto + ".");
        return cliente;
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
        lblCliente.setText("");
        txtPrecio.clear();
        lblMensaje.setText("");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}