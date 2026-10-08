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
import org.sga.manager.SessionContext;
import org.sga.model.Usuario;
import org.sga.system.Main;

public class AsesorDashboardController implements Initializable {

    @FXML
    private Label lblBienvenida;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Usuario actual = SessionContext.getInstancia().getUsuarioActual();
        lblBienvenida.setText("Bienvenido, " + (actual != null ? actual.getUsername() : "asesor"));
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
    
    @FXML
    public void eventoBuscarVehiculos(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/BuscarVehiculosView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    public void eventoRegistrarVenta(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/VentaView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }

    @FXML
    public void eventoHistorialVentas(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/HistorialVentasView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }
        @FXML
    public void eventoRegistrarAlquiler(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/AlquilerView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }
    
}
