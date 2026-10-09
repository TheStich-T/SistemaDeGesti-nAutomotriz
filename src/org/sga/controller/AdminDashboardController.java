package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.sga.dao.ReporteDAO;
import org.sga.dao.impl.ReporteDAOImpl;
import org.sga.manager.SessionContext;
import org.sga.model.Indicadores;
import org.sga.model.Usuario;
import org.sga.system.Main;

public class AdminDashboardController implements Initializable {

    private static final String SIN_DATO = "-";

    @FXML
    private Label lblBienvenida;
    @FXML
    private Label lblUsuarioSidebar;
    @FXML
    private Label lblVentasHoy;
    @FXML
    private Label lblMontoVentasMes;
    @FXML
    private Label lblAlquileresActivos;
    @FXML
    private Label lblAlquileresAtrasados;
    @FXML
    private Label lblVehiculosDisponibles;
    @FXML
    private Label lblVehiculosTaller;
    @FXML
    private Label lblVehiculosVendidos;
    @FXML
    private Label lblUsuariosActivos;

    private ReporteDAO reporteDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reporteDAO = new ReporteDAOImpl();

        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        String nombreUsuario = actual != null ? actual.getUsername() : "administrador";
        lblBienvenida.setText("Bienvenido, " + nombreUsuario);
        lblUsuarioSidebar.setText(nombreUsuario);

        cargarIndicadores();
    }

    private void cargarIndicadores() {
        Indicadores indicadores = reporteDAO.obtenerIndicadores();
        if (indicadores == null) {
            lblVentasHoy.setText(SIN_DATO);
            lblMontoVentasMes.setText(SIN_DATO);
            lblAlquileresActivos.setText(SIN_DATO);
            lblAlquileresAtrasados.setText(SIN_DATO);
            lblVehiculosDisponibles.setText(SIN_DATO);
            lblVehiculosTaller.setText(SIN_DATO);
            lblVehiculosVendidos.setText(SIN_DATO);
            lblUsuariosActivos.setText(SIN_DATO);
            return;
        }
        lblVentasHoy.setText(String.valueOf(indicadores.getVentasHoy()));
        lblMontoVentasMes.setText(String.format("Q %,.2f", indicadores.getMontoVentasMes()));
        lblAlquileresActivos.setText(String.valueOf(indicadores.getAlquileresActivos()));
        lblAlquileresAtrasados.setText(String.valueOf(indicadores.getAlquileresAtrasados()));
        lblVehiculosDisponibles.setText(String.valueOf(indicadores.getVehiculosDisponibles()));
        lblVehiculosTaller.setText(String.valueOf(indicadores.getVehiculosEnTaller()));
        lblVehiculosVendidos.setText(String.valueOf(indicadores.getVehiculosVendidos()));
        lblUsuariosActivos.setText(String.valueOf(indicadores.getUsuariosActivos()));
    }

    private void irA(String rutaFXML) {
        try {
            Main.cambiarEscena(rutaFXML);
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    public void eventoActualizarIndicadores(ActionEvent evento) {
        cargarIndicadores();
    }

    @FXML
    public void eventoReportes(ActionEvent evento) {
        irA("/org/sga/view/ReportesView.fxml");
    }

    @FXML
    public void eventoGestionUsuarios(ActionEvent evento) {
        irA("/org/sga/view/GestionUsuariosView.fxml");
    }

    @FXML
    public void eventoInventario(ActionEvent evento) {
        irA("/org/sga/view/BuscarVehiculosView.fxml");
    }

    @FXML
    public void eventoRegistrarVehiculo(ActionEvent evento) {
        irA("/org/sga/view/VehiculoAltaView.fxml");
    }

    @FXML
    public void eventoTaller(ActionEvent evento) {
        irA("/org/sga/view/MecanicoDashboardView.fxml");
    }

    @FXML
    public void eventoRegistrarVenta(ActionEvent evento) {
        irA("/org/sga/view/VentaView.fxml");
    }

    @FXML
    public void eventoHistorialVentas(ActionEvent evento) {
        irA("/org/sga/view/HistorialVentasView.fxml");
    }

    @FXML
    public void eventoRegistrarAlquiler(ActionEvent evento) {
        irA("/org/sga/view/AlquilerView.fxml");
    }

    @FXML
    public void eventoRegistrarDevolucion(ActionEvent evento) {
        irA("/org/sga/view/DevolucionView.fxml");
    }

    @FXML
    public void eventoHistorialAlquileres(ActionEvent evento) {
        irA("/org/sga/view/MisAlquileresView.fxml");
    }

    @FXML
    public void eventoCerrarSesion(ActionEvent evento) {
        try {
            SessionContext.getInstancia().cerrarSesion();
            Main.cambiarEscena("/org/sga/view/LoginView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }
}