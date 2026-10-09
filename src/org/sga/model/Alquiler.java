package org.sga.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Alquiler {

    private int id;
    private int idVehiculo;
    private long cuiCliente;
    private int idAsesor;
    private LocalDate fechaSalida;
    private LocalDate fechaRegreso;
    private boolean llevaSeguro;
    private LocalDate fechaDevolucionReal;
    private LocalDateTime fechaRegistro;
    // datos de apoyo para mostrar "Mis alquileres" (vienen de los JOIN)
    private String placa;
    private String descripcionVehiculo;
    private String nombreCliente;

    public Alquiler() {
    }

    public Alquiler(int id, int idVehiculo, long cuiCliente, int idAsesor,
            LocalDate fechaSalida, LocalDate fechaRegreso, boolean llevaSeguro,
            LocalDate fechaDevolucionReal, LocalDateTime fechaRegistro) {
        this.id = id;
        this.idVehiculo = idVehiculo;
        this.cuiCliente = cuiCliente;
        this.idAsesor = idAsesor;
        this.fechaSalida = fechaSalida;
        this.fechaRegreso = fechaRegreso;
        this.llevaSeguro = llevaSeguro;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.fechaRegistro = fechaRegistro;
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

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaRegreso() {
        return fechaRegreso;
    }

    public void setFechaRegreso(LocalDate fechaRegreso) {
        this.fechaRegreso = fechaRegreso;
    }

    public boolean isLlevaSeguro() {
        return llevaSeguro;
    }

    public void setLlevaSeguro(boolean llevaSeguro) {
        this.llevaSeguro = llevaSeguro;
    }

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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