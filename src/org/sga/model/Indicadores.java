package org.sga.model;

import java.math.BigDecimal;

public class Indicadores {

    private int ventasHoy;
    private BigDecimal montoVentasMes;
    private int alquileresActivos;
    private int alquileresAtrasados;
    private int vehiculosDisponibles;
    private int vehiculosEnTaller;
    private int vehiculosVendidos;
    private int usuariosActivos;

    public Indicadores() {
    }

    public Indicadores(int ventasHoy, BigDecimal montoVentasMes, int alquileresActivos,
            int alquileresAtrasados, int vehiculosDisponibles, int vehiculosEnTaller,
            int vehiculosVendidos, int usuariosActivos) {
        this.ventasHoy = ventasHoy;
        this.montoVentasMes = montoVentasMes;
        this.alquileresActivos = alquileresActivos;
        this.alquileresAtrasados = alquileresAtrasados;
        this.vehiculosDisponibles = vehiculosDisponibles;
        this.vehiculosEnTaller = vehiculosEnTaller;
        this.vehiculosVendidos = vehiculosVendidos;
        this.usuariosActivos = usuariosActivos;
    }

    public int getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(int ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public BigDecimal getMontoVentasMes() {
        return montoVentasMes;
    }

    public void setMontoVentasMes(BigDecimal montoVentasMes) {
        this.montoVentasMes = montoVentasMes;
    }

    public int getAlquileresActivos() {
        return alquileresActivos;
    }

    public void setAlquileresActivos(int alquileresActivos) {
        this.alquileresActivos = alquileresActivos;
    }

    public int getAlquileresAtrasados() {
        return alquileresAtrasados;
    }

    public void setAlquileresAtrasados(int alquileresAtrasados) {
        this.alquileresAtrasados = alquileresAtrasados;
    }

    public int getVehiculosDisponibles() {
        return vehiculosDisponibles;
    }

    public void setVehiculosDisponibles(int vehiculosDisponibles) {
        this.vehiculosDisponibles = vehiculosDisponibles;
    }

    public int getVehiculosEnTaller() {
        return vehiculosEnTaller;
    }

    public void setVehiculosEnTaller(int vehiculosEnTaller) {
        this.vehiculosEnTaller = vehiculosEnTaller;
    }

    public int getVehiculosVendidos() {
        return vehiculosVendidos;
    }

    public void setVehiculosVendidos(int vehiculosVendidos) {
        this.vehiculosVendidos = vehiculosVendidos;
    }

    public int getUsuariosActivos() {
        return usuariosActivos;
    }

    public void setUsuariosActivos(int usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
    }
}