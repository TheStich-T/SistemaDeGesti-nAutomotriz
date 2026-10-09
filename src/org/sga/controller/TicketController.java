package org.sga.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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

public class TicketController {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblNumero;
    @FXML private Label lblFecha;
    @FXML private Label lblCliente;
    @FXML private Label lblCui;
    @FXML private Label lblVehiculo;
    @FXML private Label lblPlaca;
    @FXML private Label lblSalida;
    @FXML private Label lblRegreso;
    @FXML private Label lblSeguro;
    @FXML private Label lblAsesor;

    // abre el ticket en una ventana aparte (solo se muestra, no se imprime)
    public static void mostrar(int numero, LocalDateTime fechaRegistro, String cliente, long cui,
            String placa, String vehiculo, LocalDate fechaSalida, LocalDate fechaRegreso,
            boolean llevaSeguro, String asesor) throws IOException {
        URL recurso = TicketController.class.getResource("/org/sga/view/TicketView.fxml");
        if (recurso == null) {
            throw new IOException("No se encontró la vista del ticket.");
        }
        FXMLLoader cargador = new FXMLLoader(recurso);
        Parent raiz = cargador.load();
        TicketController controlador = cargador.getController();
        controlador.cargarDatos(numero, fechaRegistro, cliente, cui, placa, vehiculo,
                fechaSalida, fechaRegreso, llevaSeguro, asesor);

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Ticket de alquiler");
        ventana.setResizable(false);
        ventana.setScene(new Scene(raiz));
        ventana.showAndWait();
    }

    private void cargarDatos(int numero, LocalDateTime fechaRegistro, String cliente, long cui,
            String placa, String vehiculo, LocalDate fechaSalida, LocalDate fechaRegreso,
            boolean llevaSeguro, String asesor) {
        lblNumero.setText(String.format("%06d", numero));
        lblFecha.setText(fechaRegistro != null ? fechaRegistro.format(FORMATO_FECHA_HORA) : "");
        lblCliente.setText(cliente);
        lblCui.setText(String.valueOf(cui));
        lblVehiculo.setText(vehiculo);
        lblPlaca.setText(placa);
        lblSalida.setText(fechaSalida != null ? fechaSalida.format(FORMATO_FECHA) : "");
        lblRegreso.setText(fechaRegreso != null ? fechaRegreso.format(FORMATO_FECHA) : "");
        lblSeguro.setText(llevaSeguro ? "Sí" : "No");
        lblAsesor.setText(asesor);
    }

    @FXML
    public void eventoCerrar(ActionEvent evento) {
        ((Node) evento.getSource()).getScene().getWindow().hide();
    }
}