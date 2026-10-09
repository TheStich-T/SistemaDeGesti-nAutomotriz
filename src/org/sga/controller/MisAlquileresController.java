package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.sga.dao.AlquilerDAO;
import org.sga.dao.impl.AlquilerDAOImpl;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Alquiler;
import org.sga.model.Usuario;
import org.sga.system.Main;

public class MisAlquileresController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<Alquiler> tblAlquileres;
    @FXML private TableColumn<Alquiler, String> colNumero;
    @FXML private TableColumn<Alquiler, String> colPlaca;
    @FXML private TableColumn<Alquiler, String> colVehiculo;
    @FXML private TableColumn<Alquiler, String> colCliente;
    @FXML private TableColumn<Alquiler, String> colSalida;
    @FXML private TableColumn<Alquiler, String> colRegreso;
    @FXML private TableColumn<Alquiler, String> colSeguro;
    @FXML private Label lblResumen;

    private AlquilerDAO alquilerDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        alquilerDAO = new AlquilerDAOImpl();

        colNumero.setCellValueFactory(dato -> new SimpleStringProperty(String.valueOf(dato.getValue().getId())));
        colPlaca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getPlaca()));
        colVehiculo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getDescripcionVehiculo()));
        colCliente.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombreCliente()));
        colSalida.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getFechaSalida() != null
                ? dato.getValue().getFechaSalida().format(FORMATO_FECHA) : ""));
        colRegreso.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getFechaRegreso() != null
                ? dato.getValue().getFechaRegreso().format(FORMATO_FECHA) : ""));
        colSeguro.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().isLlevaSeguro() ? "Sí" : "No"));

        cargarAlquileres();
    }

    // solo los alquileres del asesor que tiene la sesión activa
    private void cargarAlquileres() {
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();

        if (actual == null) {
            tblAlquileres.setItems(FXCollections.observableArrayList());
            lblResumen.setText("No hay una sesión activa. Inicia sesión nuevamente.");
            return;
        }

        List<Alquiler> alquileres = alquilerDAO.listarPorAsesor(actual.getId());
        tblAlquileres.setItems(FXCollections.observableArrayList(alquileres));
        lblResumen.setText(alquileres.size() + " alquiler(es)");
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        cargarAlquileres();
    }

    @FXML
    public void eventoMostrarTicket(ActionEvent evento) {
        Alquiler alquiler = tblAlquileres.getSelectionModel().getSelectedItem();
        if (alquiler == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un alquiler de la tabla.", ButtonType.OK).show();
            return;
        }
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        try {
            TicketController.mostrar(alquiler.getId(), alquiler.getFechaRegistro(), alquiler.getNombreCliente(),
                    alquiler.getCuiCliente(), alquiler.getPlaca(), alquiler.getDescripcionVehiculo(),
                    alquiler.getFechaSalida(), alquiler.getFechaRegreso(), alquiler.isLlevaSeguro(),
                    actual != null ? actual.getUsername() : "");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
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
}