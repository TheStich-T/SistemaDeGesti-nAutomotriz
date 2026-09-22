package org.sga.system;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application { 
    @Override
    public void start(Stage escenarioPrincipal) throws Exception {
        VBox raiz = new VBox();
        Scene escena = new Scene(raiz);
        
        escenarioPrincipal.setTitle("Sistema De Gestión Automotriz (SGA)");
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.show();
    }
         public static void main (String[] args) {
        launch(args);
     }
}