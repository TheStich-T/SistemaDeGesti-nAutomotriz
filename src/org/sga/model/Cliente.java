package org.sga.model;

public class Cliente {

    private long cui;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private String licencia;

    public Cliente() {
    }

    public Cliente(long cui, String nombres, String apellidos, String telefono,
            String correo, String licencia) {
        this.cui = cui;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
        this.licencia = licencia;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public long getCui() {
        return cui;
    }

    public void setCui(long cui) {
        this.cui = cui;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }
}