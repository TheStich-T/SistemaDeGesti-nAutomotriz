package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FacturaController {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblNumero;
    @FXML private Label lblFecha;
    @FXML private Label lblCliente;
    @FXML private Label lblCui;
    @FXML private Label lblVehiculo;
    @FXML private Label lblPlaca;
    @FXML private Label lblAsesor;
    @FXML private Label lblTotal;

    // abre la factura en una ventana aparte (solo se muestra, no se imprime)
    public static void mostrar(int numero, LocalDateTime fecha, String cliente, long cui,
            String placa, String vehiculo, BigDecimal precio, String asesor) throws IOException {
        URL recurso = FacturaController.class.getResource("/org/sga/view/FacturaView.fxml");
        if (recurso == null) {
            throw new IOException("No se encontró la vista de la factura.");
        }
        FXMLLoader cargador = new FXMLLoader(recurso);
        Parent raiz = cargador.load();
        FacturaController controlador = cargador.getController();
        controlador.cargarDatos(numero, fecha, cliente, cui, placa, vehiculo, precio, asesor);

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Factura de venta");
        ventana.setResizable(false);
        ventana.setScene(new Scene(raiz));
        ventana.showAndWait();
    }

    private void cargarDatos(int numero, LocalDateTime fecha, String cliente, long cui,
            String placa, String vehiculo, BigDecimal precio, String asesor) {
        lblNumero.setText(String.format("%06d", numero));
        lblFecha.setText(fecha != null ? fecha.format(FORMATO_FECHA_HORA) : "");
        lblCliente.setText(cliente);
        lblCui.setText(String.valueOf(cui));
        lblVehiculo.setText(vehiculo);
        lblPlaca.setText(placa);
        lblAsesor.setText(asesor);
        lblTotal.setText(String.format("Q %,.2f", precio));
    }

    @FXML
    public void eventoCerrar(ActionEvent evento) {
        ((Node) evento.getSource()).getScene().getWindow().hide();
    }
}