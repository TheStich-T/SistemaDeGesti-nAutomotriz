package org.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Vehiculo {

    private int id;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private String condicion;
    private String proveedor;
    private BigDecimal costo;
    private String observaciones;
    private String estado;
    private String operacionPermitida;
    private String progresoTaller;
    private int idUsuarioProvisionador;
    private LocalDateTime fechaIngreso;

    public Vehiculo() {
    }

    public Vehiculo(int id, String placa, String marca, String modelo, int anio, String color,
            String condicion, String proveedor, BigDecimal costo, String observaciones,
            String estado, String operacionPermitida, String progresoTaller,
            int idUsuarioProvisionador, LocalDateTime fechaIngreso) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.condicion = condicion;
        this.proveedor = proveedor;
        this.costo = costo;
        this.observaciones = observaciones;
        this.estado = estado;
        this.operacionPermitida = operacionPermitida;
        this.progresoTaller = progresoTaller;
        this.idUsuarioProvisionador = idUsuarioProvisionador;
        this.fechaIngreso = fechaIngreso;
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // US-2.2 (T2.8): getter y setter del campo operacionPermitida
    public String getOperacionPermitida() {
        return operacionPermitida;
    }

    public void setOperacionPermitida(String operacionPermitida) {
        this.operacionPermitida = operacionPermitida;
    }

    public String getProgresoTaller() {
        return progresoTaller;
    }

    public void setProgresoTaller(String progresoTaller) {
        this.progresoTaller = progresoTaller;
    }

    public int getIdUsuarioProvisionador() {
        return idUsuarioProvisionador;
    }

    public void setIdUsuarioProvisionador(int idUsuarioProvisionador) {
        this.idUsuarioProvisionador = idUsuarioProvisionador;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
