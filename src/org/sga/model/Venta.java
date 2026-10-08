package org.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venta {

    private int id;
    private int idVehiculo;
    private long cuiCliente;
    private int idAsesor;
    private BigDecimal precio;
    private LocalDateTime fechaVenta;
    // datos de apoyo para mostrar el historial (vienen de los JOIN)
    private String placa;
    private String descripcionVehiculo;
    private String nombreCliente;

    public Venta() {
    }

    public Venta(int id, int idVehiculo, long cuiCliente, int idAsesor,
            BigDecimal precio, LocalDateTime fechaVenta) {
        this.id = id;
        this.idVehiculo = idVehiculo;
        this.cuiCliente = cuiCliente;
        this.idAsesor = idAsesor;
        this.precio = precio;
        this.fechaVenta = fechaVenta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(int idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public long getCuiCliente() {
        return cuiCliente;
    }

    public void setCuiCliente(long cuiCliente) {
        this.cuiCliente = cuiCliente;
    }

    public int getIdAsesor() {
        return idAsesor;
    }

    public void setIdAsesor(int idAsesor) {
        this.idAsesor = idAsesor;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getDescripcionVehiculo() {
        return descripcionVehiculo;
    }

    public void setDescripcionVehiculo(String descripcionVehiculo) {
        this.descripcionVehiculo = descripcionVehiculo;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
}