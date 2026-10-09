package org.sga.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.sga.dao.ReporteDAO;
import org.sga.dao.impl.ReporteDAOImpl;
import org.sga.exception.ValidarException;
import org.sga.model.FilaReporte;
import org.sga.model.PeriodoReporte;
import org.sga.system.Main;

public class ReportesController implements Initializable {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private ComboBox<PeriodoReporte> cmbPeriodo;
    @FXML private DatePicker dtpReferencia;
    @FXML private Label lblRango;
    @FXML private Label lblResumen;

    @FXML private TableView<FilaReporte> tblVentas;
    @FXML private TableColumn<FilaReporte, String> colVentaNumero;
    @FXML private TableColumn<FilaReporte, String> colVentaPlaca;
    @FXML private TableColumn<FilaReporte, String> colVentaVehiculo;
    @FXML private TableColumn<FilaReporte, String> colVentaCliente;
    @FXML private TableColumn<FilaReporte, String> colVentaAsesor;
    @FXML private TableColumn<FilaReporte, String> colVentaFecha;
    @FXML private TableColumn<FilaReporte, String> colVentaMonto;

    @FXML private TableView<FilaReporte> tblAlquileres;
    @FXML private TableColumn<FilaReporte, String> colAlqNumero;
    @FXML private TableColumn<FilaReporte, String> colAlqPlaca;
    @FXML private TableColumn<FilaReporte, String> colAlqVehiculo;
    @FXML private TableColumn<FilaReporte, String> colAlqCliente;
    @FXML private TableColumn<FilaReporte, String> colAlqAsesor;
    @FXML private TableColumn<FilaReporte, String> colAlqSalida;
    @FXML private TableColumn<FilaReporte, String> colAlqRegreso;
    @FXML private TableColumn<FilaReporte, String> colAlqTotal;
    @FXML private TableColumn<FilaReporte, String> colAlqEstado;

    private ReporteDAO reporteDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reporteDAO = new ReporteDAOImpl();

        cmbPeriodo.setItems(FXCollections.observableArrayList(PeriodoReporte.values()));
        cmbPeriodo.setValue(PeriodoReporte.DIA);
        dtpReferencia.setValue(LocalDate.now());

        configurarColumnasVentas();
        configurarColumnasAlquileres();
        generarReporte();
    }

    private void configurarColumnasVentas() {
        colVentaNumero.setCellValueFactory(dato -> new SimpleStringProperty(String.valueOf(dato.getValue().getId())));
        colVentaPlaca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getPlaca()));
        colVentaVehiculo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getVehiculo()));
        colVentaCliente.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getCliente()));
        colVentaAsesor.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getAsesor()));
        colVentaFecha.setCellValueFactory(dato -> new SimpleStringProperty(formatearFecha(dato.getValue().getFecha())));
        colVentaMonto.setCellValueFactory(dato -> new SimpleStringProperty(formatearMonto(dato.getValue().getMonto())));
    }

    private void configurarColumnasAlquileres() {
        colAlqNumero.setCellValueFactory(dato -> new SimpleStringProperty(String.valueOf(dato.getValue().getId())));
        colAlqPlaca.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getPlaca()));
        colAlqVehiculo.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getVehiculo()));
        colAlqCliente.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getCliente()));
        colAlqAsesor.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getAsesor()));
        colAlqSalida.setCellValueFactory(dato -> new SimpleStringProperty(formatearFecha(dato.getValue().getFecha())));
        colAlqRegreso.setCellValueFactory(dato -> new SimpleStringProperty(formatearFecha(dato.getValue().getFechaFin())));
        colAlqTotal.setCellValueFactory(dato -> new SimpleStringProperty(formatearMonto(dato.getValue().getMonto())));
        colAlqEstado.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getEstado()));
    }

    // T3.34: calcula el rango según el período y la fecha de referencia, T3.35: consulta ventas y alquileres de ese rango
    private void generarReporte() {
        PeriodoReporte periodo = cmbPeriodo.getValue();
        LocalDate referencia = dtpReferencia.getValue();
        try {
            ValidarException.validarNulo(periodo, "Selecciona el período del reporte.");
            ValidarException.validarNulo(referencia, "Selecciona la fecha de referencia.");
        } catch (ValidarException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
            return;
        }

        LocalDate desde = periodo.desde(referencia);
        LocalDate hasta = periodo.hasta(referencia);
        List<FilaReporte> ventas = reporteDAO.reporteVentas(desde, hasta);
        List<FilaReporte> alquileres = reporteDAO.reporteAlquileres(desde, hasta);
        tblVentas.setItems(FXCollections.observableArrayList(ventas));
        tblAlquileres.setItems(FXCollections.observableArrayList(alquileres));
        lblRango.setText(desde.equals(hasta)? "Reporte del " + desde.format(FORMATO_FECHA): "Reporte del " + desde.format(FORMATO_FECHA) + " al " + hasta.format(FORMATO_FECHA));
        lblResumen.setText("Ventas: " + ventas.size() + " (" + formatearMonto(sumar(ventas)) + ")" + "     |     Alquileres: " + alquileres.size() + " (" + formatearMonto(sumar(alquileres)) + ")");
    }

    private BigDecimal sumar(List<FilaReporte> filas) {
        return filas.stream().map(FilaReporte::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }

    private String formatearMonto(BigDecimal monto) {
        return String.format("Q %,.2f", monto != null ? monto : BigDecimal.ZERO);
    }

    @FXML
    public void eventoGenerar(ActionEvent evento) {
        generarReporte();
    }

    @FXML
    public void eventoVolver(ActionEvent evento) {
        try {
            Main.cambiarEscena("/org/sga/view/AdminDashboardView.fxml");
        } catch (IOException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).show();
        }
    }
}