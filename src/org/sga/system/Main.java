package org.sga.system;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String LOGIN_VIEW = "/org/sga/view/LoginView.fxml";
    private static Stage escenarioPrincipal;

    public static void cambiarEscena(String rutaFXML) throws IOException {
        URL recurso = Main.class.getResource(rutaFXML);
        if (recurso == null) {
            throw new IOException("No se encontró la vista: " + rutaFXML);
        }
        Parent raiz = FXMLLoader.load(recurso);
        escenarioPrincipal.setScene(new Scene(raiz));
        escenarioPrincipal.sizeToScene();
        escenarioPrincipal.centerOnScreen();
        escenarioPrincipal.show();
    }

    @Override
    public void start(Stage escenario) throws Exception {
        Main.escenarioPrincipal = escenario;
        escenario.setTitle("Sistema De Gestión Automotriz (SGA)");
        cambiarEscena(LOGIN_VIEW);
    }

    public static void main(String[] args) {
        launch(args);
    }
}