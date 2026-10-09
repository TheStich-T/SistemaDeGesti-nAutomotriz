package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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

public class DevolucionController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<Alquiler> tblAlquileres;
    @FXML private TableColumn<Alquiler, String> colNumero;
    @FXML private TableColumn<Alquiler, String> colPlaca;
    @FXML private TableColumn<Alquiler, String> colVehiculo;
    @FXML private TableColumn<Alquiler, String> colCliente;
    @FXML private TableColumn<Alquiler, String> colSalida;
    @FXML private TableColumn<Alquiler, String> colRegreso;
    @FXML private TableColumn<Alquiler, String> colEstado;
    @FXML private TableColumn<Alquiler, String> colAtraso;
    @FXML private TableColumn<Alquiler, String> colCobro;
    @FXML private Label lblDetalle;
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
        colEstado.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getDiasAtraso() > 0 ? "Atrasado" : "A tiempo"));
        colAtraso.setCellValueFactory(dato -> new SimpleStringProperty(
                String.valueOf(dato.getValue().getDiasAtraso())));
        colCobro.setCellValueFactory(dato -> new SimpleStringProperty(
                formatearMonto(dato.getValue().getCobroAdicional())));

        // al seleccionar un alquiler se muestra su detalle y el cobro adicional si va atrasado
        tblAlquileres.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            mostrarDetalle(seleccionado);
        });

        cargarAlquileres();
    }

    // T3.23: alquileres activos (los que todavía no se han devuelto)
    private void cargarAlquileres() {
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();

        if (actual == null) {
            tblAlquileres.setItems(FXCollections.observableArrayList());
            lblResumen.setText("No hay una sesión activa. Inicia sesión nuevamente.");
            return;
        }

        List<Alquiler> alquileres = alquilerDAO.listarActivos();
        tblAlquileres.setItems(FXCollections.observableArrayList(alquileres));

        long atrasados = alquileres.stream().filter(alquiler -> alquiler.getDiasAtraso() > 0).count();
        lblResumen.setText(alquileres.size() + " alquiler(es) activo(s)  |  " + atrasados + " atrasado(s)");
        mostrarDetalle(null);
    }

    private void mostrarDetalle(Alquiler alquiler) {
        lblDetalle.getStyleClass().setAll("field-label");
        if (alquiler == null) {
            lblDetalle.setText("Selecciona el alquiler que se va a devolver.");
            return;
        }
        String detalle = "Alquiler " + String.format("%06d", alquiler.getId()) + "  |  "
                + alquiler.getDescripcionVehiculo() + " (" + alquiler.getPlaca() + ")  |  Cliente: "
                + alquiler.getNombreCliente() + "  |  Devolución programada: "
                + (alquiler.getFechaRegreso() != null ? alquiler.getFechaRegreso().format(FORMATO_FECHA) : "");
        if (alquiler.getDiasAtraso() > 0) {
            // T3.28: cobro adicional = días de atraso x tarifa por día del alquiler
            detalle += "\nATRASADO: " + alquiler.getDiasAtraso() + " día(s) x "
                    + formatearMonto(alquiler.getPrecioDia()) + " = cobro adicional de "
                    + formatearMonto(alquiler.getCobroAdicional());
            lblDetalle.getStyleClass().setAll("error-label");
        } else {
            detalle += "\nDevolución a tiempo, sin cobro adicional.";
        }
        lblDetalle.setText(detalle);
    }

    @FXML
    public void eventoRegistrarDevolucion(ActionEvent evento) {
        Alquiler alquiler = tblAlquileres.getSelectionModel().getSelectedItem();
        if (alquiler == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un alquiler de la tabla.");
            return;
        }

        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        if (actual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "No hay una sesión activa. Inicia sesión nuevamente.");
            return;
        }

        String mensaje = "¿Registrar la devolución del vehículo " + alquiler.getPlaca() + " de "
                + alquiler.getNombreCliente() + "?";
        if (alquiler.getDiasAtraso() > 0) {
            mensaje += "\n\nVa atrasado " + alquiler.getDiasAtraso() + " día(s). Cobro adicional: "
                    + formatearMonto(alquiler.getCobroAdicional());
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setHeaderText("Confirmar devolución");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        // la fecha y hora, el estado (devuelto / atrasado) y el cobro los calcula y guarda la base de datos
        if (alquilerDAO.registrarDevolucion(alquiler)) {
            boolean atrasado = "atrasado".equals(alquiler.getEstadoDevolucion());
            StringBuilder resumen = new StringBuilder();
            resumen.append("Alquiler ").append(String.format("%06d", alquiler.getId()))
                    .append(" - Placa ").append(alquiler.getPlaca()).append("\n");
            resumen.append("Cliente: ").append(alquiler.getNombreCliente()).append("\n");
            resumen.append("Fecha y hora de devolución: ")
                    .append(alquiler.getFechaHoraDevolucion() != null
                            ? alquiler.getFechaHoraDevolucion().format(FORMATO_FECHA_HORA) : "").append("\n");
            if (atrasado) {
                resumen.append("Estado: Atrasado (").append(alquiler.getDiasAtraso()).append(" día(s) de atraso)\n");
            } else {
                resumen.append("Estado: Devuelto a tiempo\n");
            }
            resumen.append("Cobro adicional: ").append(formatearMonto(alquiler.getCobroAdicional())).append("\n");
            resumen.append("El vehículo quedó Disponible.");

            Alert exito = new Alert(Alert.AlertType.INFORMATION, resumen.toString(), ButtonType.OK);
            exito.setHeaderText("Devolución registrada");
            exito.showAndWait();
            cargarAlquileres();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar la devolución. "
                    + "Verifica que el alquiler siga activo.");
            cargarAlquileres();
        }
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        cargarAlquileres();
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

    private String formatearMonto(BigDecimal monto) {
        return String.format("Q %,.2f", monto != null ? monto : BigDecimal.ZERO);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        new Alert(tipo, mensaje, ButtonType.OK).show();
    }
}