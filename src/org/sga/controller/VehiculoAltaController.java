package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Year;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.sga.dao.VehiculoDAO;
import org.sga.dao.impl.VehiculoDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.model.Vehiculo;
import org.sga.system.Main;

public class VehiculoAltaController implements Initializable {

    private static final String DESTINO_DISPONIBLE = "Disponible";
    private static final String DESTINO_TALLER = "Cola de mecánico";
    private static final BigDecimal COSTO_MAXIMO = new BigDecimal("99999999.99");
    private static final int ANIO_MINIMO = 1950;

    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, String> colPlaca;
    @FXML private TableColumn<Vehiculo, String> colMarca;
    @FXML private TableColumn<Vehiculo, String> colModelo;
    @FXML private TableColumn<Vehiculo, String> colAnio;
    @FXML private TableColumn<Vehiculo, String> colCondicion;
    @FXML private TableColumn<Vehiculo, String> colCosto;
    @FXML private TableColumn<Vehiculo, String> colEstado;

    @FXML private TextField txtPlaca;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private TextField txtAnio;
    @FXML private TextField txtColor;
    @FXML private ComboBox<String> cmbCondicion;
    @FXML private TextField txtProveedor;
    @FXML private TextField txtCosto;
    @FXML private ComboBox<String> cmbDestino;
    @FXML private TextField txtObservaciones;
    @FXML private Label lblMensaje;

    private VehiculoDAO vehiculoDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vehiculoDAO = new VehiculoDAOImpl();
        cmbCondicion.setItems(FXCollections.observableArrayList("nuevo", "usado"));
        cmbDestino.setItems(FXCollections.observableArrayList(DESTINO_DISPONIBLE, DESTINO_TALLER));
        lblMensaje.setText("");

        colPlaca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getPlaca()));
        colMarca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getMarca()));
        colModelo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getModelo()));
        colAnio.setCellValueFactory(dato -> new SimpleStringProperty(String.valueOf(dato.getValue().getAnio())));
        colCondicion.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getCondicion()));
        colCosto.setCellValueFactory(dato
                -> new SimpleStringProperty(String.format("Q %,.2f", dato.getValue().getCosto())));
        colEstado.setCellValueFactory(dato
                -> new SimpleStringProperty(textoEstado(dato.getValue().getEstado())));

        cargarVehiculos();
    }

    private void cargarVehiculos() {
        tblVehiculos.setItems(FXCollections.observableArrayList(vehiculoDAO.listar()));
    }

    @FXML
    public void eventoRegistrar(ActionEvent evento) {
        try {
           
            ValidarException.validarNoVacio(txtPlaca.getText(), "placa");
            ValidarException.validarNoVacio(txtMarca.getText(), "marca");
            ValidarException.validarNoVacio(txtModelo.getText(), "modelo");
            ValidarException.validarNoVacio(txtAnio.getText(), "año");
            ValidarException.validarNoVacio(txtCosto.getText(), "precio / costo");
            ValidarException.validarNulo(cmbCondicion.getValue(), "Debe seleccionar la condición del vehículo.");
            ValidarException.validarNulo(cmbDestino.getValue(), "Debe seleccionar el destino inicial del vehículo.");

            String placa = txtPlaca.getText().trim().toUpperCase();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String color = txtColor.getText().trim();
            String proveedor = txtProveedor.getText().trim();
            String observaciones = txtObservaciones.getText().trim();

            if (placa.matches(".*\\s.*")) {
                throw new ValidarException("La placa no puede contener espacios.");
            }
            if (placa.length() > 15) {
                throw new ValidarException("La placa no puede tener más de 15 caracteres.");
            }
            if (marca.length() > 50) {
                throw new ValidarException("La marca no puede tener más de 50 caracteres.");
            }
            if (modelo.length() > 50) {
                throw new ValidarException("El modelo no puede tener más de 50 caracteres.");
            }
            if (color.length() > 30) {
                throw new ValidarException("El color no puede tener más de 30 caracteres.");
            }
            if (proveedor.length() > 100) {
                throw new ValidarException("El proveedor no puede tener más de 100 caracteres.");
            }

            int anio = validarAnio(txtAnio.getText().trim());
            BigDecimal costo = validarCosto(txtCosto.getText().trim());

            if (vehiculoDAO.buscarPorPlaca(placa) != null) {
                throw new ValidarException("Ya existe un vehículo registrado con la placa " + placa + ".");
            }

            Usuario actual = SessionContext.getInstancia().getUsuarioActual();
            ValidarException.validarNulo(actual, "No hay una sesión activa. Inicia sesión nuevamente.");
            
            Vehiculo nuevoVehiculo = new Vehiculo();
            nuevoVehiculo.setPlaca(placa);
            nuevoVehiculo.setMarca(marca);
            nuevoVehiculo.setModelo(modelo);
            nuevoVehiculo.setAnio(anio);
            nuevoVehiculo.setColor(color.isEmpty() ? null : color);
            nuevoVehiculo.setCondicion(cmbCondicion.getValue());
            nuevoVehiculo.setProveedor(proveedor.isEmpty() ? null : proveedor);
            nuevoVehiculo.setCosto(costo);
            nuevoVehiculo.setObservaciones(observaciones.isEmpty() ? null : observaciones);
            nuevoVehiculo.setEstado(DESTINO_DISPONIBLE.equals(cmbDestino.getValue()) ? "disponible" : "en_taller");
            nuevoVehiculo.setIdUsuarioProvisionador(actual.getId());

            if (vehiculoDAO.insertar(nuevoVehiculo)) {
                String destino = DESTINO_DISPONIBLE.equals(cmbDestino.getValue())
                        ? "quedó Disponible para la venta o el alquiler."
                        : "pasó a la cola del mecánico.";
                mostrarAlerta(Alert.AlertType.INFORMATION, "Vehículo " + placa + " registrado con éxito y " + destino);
                limpiarCampos();
                cargarVehiculos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar el vehículo.");
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

    private int validarAnio(String texto) throws ValidarException {
        int anio;
        try {
            anio = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new ValidarException("El año debe ser un número entero.");
        }
        int anioMaximo = Year.now().getValue() + 1;
        if (anio < ANIO_MINIMO || anio > anioMaximo) {
            throw new ValidarException("El año debe estar entre " + ANIO_MINIMO + " y " + anioMaximo + ".");
        }
        return anio;
    }

    private BigDecimal validarCosto(String texto) throws ValidarException {
        BigDecimal costo;
        try {
            costo = new BigDecimal(texto.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new ValidarException("El precio / costo debe ser un número válido.");
        }
        if (costo.signum() <= 0) {
            throw new ValidarException("El precio / costo debe ser mayor que cero.");
        }
        if (costo.scale() > 2) {
            throw new ValidarException("El precio / costo admite máximo 2 decimales.");
        }
        if (costo.compareTo(COSTO_MAXIMO) > 0) {
            throw new ValidarException("El precio / costo excede el máximo permitido.");
        }
        return costo;
    }

    private String textoEstado(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "disponible" -> "Disponible";
            case "en_taller" -> "En taller";
            case "en_alquiler" -> "En alquiler";
            case "vendido" -> "Vendido";
            default -> estado;
        };
    }

    private void limpiarCampos() {
        txtPlaca.clear();
        txtMarca.clear();
        txtModelo.clear();
        txtAnio.clear();
        txtColor.clear();
        cmbCondicion.setValue(null);
        txtProveedor.clear();
        txtCosto.clear();
        cmbDestino.setValue(null);
        txtObservaciones.clear();
        lblMensaje.setText("");
        tblVehiculos.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}