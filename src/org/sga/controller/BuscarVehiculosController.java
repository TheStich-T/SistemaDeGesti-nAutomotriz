package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.model.Vehiculo;
import org.sga.system.Main;

public class BuscarVehiculosController implements Initializable {

    private static final String OPERACION_TODAS = "Todas";
    private static final String OPERACION_VENTA = "Venta";
    private static final String OPERACION_ALQUILER = "Alquiler";

    @FXML private TextField txtCriterio;
    @FXML private ComboBox<String> cmbOperacion;
    @FXML private TableView<Vehiculo> tblVehiculos;
    @FXML private TableColumn<Vehiculo, String> colPlaca;
    @FXML private TableColumn<Vehiculo, String> colMarca;
    @FXML private TableColumn<Vehiculo, String> colModelo;
    @FXML private TableColumn<Vehiculo, String> colTipo;
    @FXML private TableColumn<Vehiculo, String> colAnio;
    @FXML private TableColumn<Vehiculo, String> colColor;
    @FXML private TableColumn<Vehiculo, String> colCondicion;
    @FXML private TableColumn<Vehiculo, String> colPrecio;
    @FXML private TableColumn<Vehiculo, String> colOperacion;
    @FXML private Label lblConteo;

    private VehiculoDAO vehiculoDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vehiculoDAO = new VehiculoDAOImpl();
        cmbOperacion.setItems(FXCollections.observableArrayList(OPERACION_TODAS, OPERACION_VENTA, OPERACION_ALQUILER));
        cmbOperacion.setValue(OPERACION_TODAS);

        colPlaca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getPlaca()));
        colMarca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getMarca()));
        colModelo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getModelo()));
        colTipo.setCellValueFactory(dato
                -> new SimpleStringProperty(textoTipo(dato.getValue().getTipo())));
        colAnio.setCellValueFactory(dato -> new SimpleStringProperty(String.valueOf(dato.getValue().getAnio())));
        colColor.setCellValueFactory(dato
                -> new SimpleStringProperty(dato.getValue().getColor() != null ? dato.getValue().getColor() : ""));
        colCondicion.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getCondicion()));
        colPrecio.setCellValueFactory(dato
                -> new SimpleStringProperty(String.format("Q %,.2f", dato.getValue().getCosto())));
        colOperacion.setCellValueFactory(dato
                -> new SimpleStringProperty(textoOperacion(dato.getValue().getOperacionPermitida())));

        // al abrir la pantalla se muestran todos los disponibles
        buscar();
    }

    private void buscar() {
        String criterio = txtCriterio.getText() == null ? "" : txtCriterio.getText().trim();
        List<Vehiculo> resultados = vehiculoDAO.buscarDisponibles(criterio, valorOperacion(cmbOperacion.getValue()));
        tblVehiculos.setItems(FXCollections.observableArrayList(resultados));
        lblConteo.setText(resultados.size() + " vehículo(s) disponible(s)");
    }

    @FXML
    public void eventoBuscar(ActionEvent evento) {
        buscar();
    }

    @FXML
    public void eventoLimpiar(ActionEvent evento) {
        txtCriterio.clear();
        cmbOperacion.setValue(OPERACION_TODAS);
        buscar();
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            Usuario actual = SessionContext.getInstancia().getUsuarioActual();
            String dashboard = (actual != null) ? RolPermisos.getDashboardPorRol(actual.getRol()) : null;
            Main.cambiarEscena(dashboard != null ? dashboard : "/org/sga/view/LoginView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }

    private String valorOperacion(String textoSeleccionado) {
        if (textoSeleccionado == null) {
            return null;
        }
        return switch (textoSeleccionado) {
            case OPERACION_VENTA -> "venta";
            case OPERACION_ALQUILER -> "alquiler";
            default -> null;
        };
    }

    private String textoOperacion(String operacion) {
        if (operacion == null) {
            return "";
        }
        return switch (operacion) {
            case "venta" -> OPERACION_VENTA;
            case "alquiler" -> OPERACION_ALQUILER;
            case "ambas" -> "Ambas";
            default -> operacion;
        };
    }

    private String textoTipo(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case "sedan" -> "Sedán";
            case "pickup" -> "Pickup";
            case "suv" -> "SUV";
            case "hatchback" -> "Hatchback";
            case "otro" -> "Otro";
            default -> tipo;
        };
    }
}