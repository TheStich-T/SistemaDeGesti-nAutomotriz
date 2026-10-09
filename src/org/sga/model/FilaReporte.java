package org.sga.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FilaReporte {

    private int id;
    private String placa;
    private String vehiculo;
    private String cliente;
    private String asesor;
    private LocalDate fecha;
    private BigDecimal monto;
    private LocalDate fechaFin;
    private String estado;

    public FilaReporte() {
    }

    public FilaReporte(int id, String placa, String vehiculo, String cliente, String asesor,
            LocalDate fecha, BigDecimal monto, LocalDate fechaFin, String estado) {
        this.id = id;
        this.placa = placa;
        this.vehiculo = vehiculo;
        this.cliente = cliente;
        this.asesor = asesor;
        this.fecha = fecha;
        this.monto = monto;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getAsesor() {
        return asesor;
    }

    public void setAsesor(String asesor) {
        this.asesor = asesor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}