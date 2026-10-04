package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.sga.dao.VehiculoDAO;
import org.sga.dao.impl.VehiculoDAOImpl;
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.model.Vehiculo;
import org.sga.system.Main;

public class MecanicoDashboardController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblBienvenida;
    @FXML private Label lblUsuarioSidebar;
    @FXML private Label lblConteoCola;
    @FXML private TableView<Vehiculo> tablaCola;
    @FXML private TableColumn<Vehiculo, Integer> columnaId;
    @FXML private TableColumn<Vehiculo, String> columnaPlaca;
    @FXML private TableColumn<Vehiculo, String> columnaMarca;
    @FXML private TableColumn<Vehiculo, String> columnaModelo;
    @FXML private TableColumn<Vehiculo, Integer> columnaAnio;
    @FXML private TableColumn<Vehiculo, String> columnaEstado;
    @FXML private TableColumn<Vehiculo, String> columnaObservaciones;
    @FXML private TableColumn<Vehiculo, String> columnaFechaIngreso;
    @FXML private ComboBox<String> comboEstado;

    private final VehiculoDAO vehiculoDAO = new VehiculoDAOImpl();
    private final ObservableList<Vehiculo> listaCola = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarEstados();
        cargarColaTaller();

        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        String nombreUsuario = actual != null ? actual.getUsername() : "mecánico";

        lblBienvenida.setText("Bienvenido, " + nombreUsuario);

        if (lblUsuarioSidebar != null) {
            lblUsuarioSidebar.setText(nombreUsuario);
        }
    }

    private void configurarTabla() {
        columnaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnaPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        columnaMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        columnaModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        columnaAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));

        // T2.16: la tabla muestra los mismos nombres que el combo
        columnaEstado.setCellValueFactory(dato
                -> new SimpleStringProperty(convertirEstadoVisible(dato.getValue().getProgresoTaller())));

        columnaObservaciones.setCellValueFactory(dato -> {
            String observaciones = dato.getValue().getObservaciones();
            return new SimpleStringProperty(observaciones != null ? observaciones : "");
        });

        columnaFechaIngreso.setCellValueFactory(dato -> {
            if (dato.getValue().getFechaIngreso() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(dato.getValue().getFechaIngreso().format(FORMATO_FECHA));
        });

        tablaCola.setItems(listaCola);

        tablaCola.getSelectionModel().selectedItemProperty().addListener((observable, anterior, seleccionado) -> {
            if (seleccionado != null) {
                comboEstado.setValue(convertirEstadoVisible(seleccionado.getProgresoTaller()));
            }
        });
    }

    private void configurarEstados() {
        comboEstado.setItems(FXCollections.observableArrayList(
                "Tiene falla",
                "Se está trabajando",
                "Trabajo terminado"
        ));
    }

    private void cargarColaTaller() {
        listaCola.clear();
        listaCola.addAll(vehiculoDAO.listarColaTaller());
        lblConteoCola.setText(listaCola.size() + " vehículo(s) en cola");
    }

    // Recarga la cola en silencio (sin popup); el conteo se ve en pantalla
    @FXML
    public void eventoMostrarCola(ActionEvent evento) {
        cargarColaTaller();
    }

    @FXML
    public void eventoCambiarEstado(ActionEvent evento) {
        Vehiculo vehiculoSeleccionado = tablaCola.getSelectionModel().getSelectedItem();

        if (vehiculoSeleccionado == null) {
            mostrarAdvertencia("Selecciona un vehículo de la cola.");
            return;
        }

        String estadoSeleccionado = comboEstado.getValue();

        if (estadoSeleccionado == null || estadoSeleccionado.isBlank()) {
            mostrarAdvertencia("Selecciona el nuevo estado del vehículo.");
            return;
        }

        String progresoBD = convertirEstadoBD(estadoSeleccionado);

        // no tiene sentido "cambiar" al mismo estado que ya tiene
        if (progresoBD.equals(vehiculoSeleccionado.getProgresoTaller())) {
            mostrarAdvertencia("El vehículo " + vehiculoSeleccionado.getPlaca()
                    + " ya tiene el estado: " + estadoSeleccionado + ".");
            return;
        }

        boolean actualizado = vehiculoDAO.actualizarProgresoTaller(vehiculoSeleccionado.getId(), progresoBD);

        if (actualizado) {
            String placa = vehiculoSeleccionado.getPlaca();
            cargarColaTaller();
            comboEstado.setValue(null);
            mostrarInformacion("Estado actualizado", "El vehículo con placa " + placa + " ahora tiene el estado: " + estadoSeleccionado);
        } else {
            mostrarError("No fue posible actualizar el estado del vehículo.");
        }
    }

    private String convertirEstadoVisible(String estadoBD) {
        if (estadoBD == null) {
            return "Tiene falla";
        }

        return switch (estadoBD) {
            case "pendiente" -> "Tiene falla";
            case "en_progreso" -> "Se está trabajando";
            case "terminado" -> "Trabajo terminado";
            default -> "Tiene falla";
        };
    }

    private String convertirEstadoBD(String estadoVisible) {
        return switch (estadoVisible) {
            case "Tiene falla" -> "pendiente";
            case "Se está trabajando" -> "en_progreso";
            case "Trabajo terminado" -> "terminado";
            default -> "pendiente";
        };
    }

    @FXML
    public void eventoProximamente(ActionEvent evento) {
        new Alert(Alert.AlertType.INFORMATION, "Esta función corresponde a la historia de regreso a Disponible.", ButtonType.OK).showAndWait();
    }

    @FXML
    public void eventoCerrarSesion(ActionEvent evento) {
        try {
            SessionContext.getInstancia().cerrarSesion();
            Main.cambiarEscena("/org/sga/view/LoginView.fxml");
        } catch (IOException e) {
            mostrarAdvertencia(e.getMessage());
        }
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK).showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK).showAndWait();
    }

    private void mostrarError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK).showAndWait();
    }
}