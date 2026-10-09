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

    // T3.32: muestra en las tarjetas los valores que devuelve sp_indicadoresgenerales
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

    @FXML
    public void eventoActualizarIndicadores(ActionEvent evento) {
        cargarIndicadores();
    }

    @FXML
    public void eventoReportes(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/ReportesView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    public void eventoGestionUsuarios(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/GestionUsuariosView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    public void eventoProximamente(ActionEvent evento) {
        new Alert(Alert.AlertType.INFORMATION, "Esta función se implementa en un próximo sprint.", ButtonType.OK).show();
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