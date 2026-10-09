package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
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
import java.util.ArrayList;
import java.util.Comparator;
import org.sga.dao.UsuarioDAO;
import org.sga.dao.impl.UsuarioDAOImpl;
import org.sga.dao.VentaDAO;
import org.sga.dao.impl.VentaDAOImpl;
import org.sga.manager.RolPermisos;
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.model.Venta;
import org.sga.system.Main;

public class HistorialVentasController implements Initializable {
    
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<Venta> tblVentas;
    @FXML private TableColumn<Venta, String> colNumero;
    @FXML private TableColumn<Venta, String> colPlaca;
    @FXML private TableColumn<Venta, String> colVehiculo;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, String> colPrecio;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private Label lblResumen;
    @FXML private Label lblTitulo;
    private UsuarioDAO usuarioDAO;

    private VentaDAO ventaDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ventaDAO = new VentaDAOImpl();
        
                usuarioDAO = new UsuarioDAOImpl();
        if (esAdmin(SessionContext.getInstancia().getUsuarioActual())) {
            lblTitulo.setText("Historial de Ventas");
        }
        colNumero.setCellValueFactory(dato -> new SimpleStringProperty(String.valueOf(dato.getValue().getId())));
        colPlaca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getPlaca()));
        colVehiculo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getDescripcionVehiculo()));
        colCliente.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombreCliente()));
        colPrecio.setCellValueFactory(dato
                -> new SimpleStringProperty(String.format("Q %,.2f", dato.getValue().getPrecio())));
        colFecha.setCellValueFactory(dato -> {
            if (dato.getValue().getFechaVenta() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(dato.getValue().getFechaVenta().format(FORMATO_FECHA));
        });

        cargarVentas();
    }

        // el asesor ve solo sus ventas; el admin ve las de todos
    private void cargarVentas() {
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();

        if (actual == null) {
            tblVentas.setItems(FXCollections.observableArrayList());
            lblResumen.setText("No hay una sesión activa. Inicia sesión nuevamente.");
            return;
        }

        List<Venta> ventas;
        if (esAdmin(actual)) {
            ventas = new ArrayList<>();
            for (Usuario usuario : usuarioDAO.listar()) {
                ventas.addAll(ventaDAO.listarPorAsesor(usuario.getId()));
            }
            ventas.sort(Comparator.comparing(Venta::getFechaVenta,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            ventas = ventaDAO.listarPorAsesor(actual.getId());
        }
        tblVentas.setItems(FXCollections.observableArrayList(ventas));

        BigDecimal total = ventas.stream()
                .map(Venta::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblResumen.setText(ventas.size() + " venta(s)  |  Total vendido: " + String.format("Q %,.2f", total));
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario != null && "admin".equalsIgnoreCase(usuario.getRol());
    }

    @FXML
    public void eventoActualizar(ActionEvent evento) {
        cargarVentas();
    }

    @FXML
    public void eventoMostrarFactura(ActionEvent evento) {
        Venta venta = tblVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una venta de la tabla.", ButtonType.OK).show();
            return;
        }
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        try {
            FacturaController.mostrar(venta.getId(), venta.getFechaVenta(), venta.getNombreCliente(),
                    venta.getCuiCliente(), venta.getPlaca(), venta.getDescripcionVehiculo(),
                    venta.getPrecio(), actual != null ? actual.getUsername() : "");
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