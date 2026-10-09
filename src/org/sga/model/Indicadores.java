package org.sga.model;

import java.math.BigDecimal;

public class Indicadores {

    private final int ventasHoy;
    private final BigDecimal montoVentasMes;
    private final int alquileresActivos;
    private final int alquileresAtrasados;
    private final int vehiculosDisponibles;
    private final int vehiculosEnTaller;
    private final int vehiculosVendidos;
    private final int usuariosActivos;
    
    public Indicadores(){
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

    public BigDecimal getMontoVentasMes() {
        return montoVentasMes;
    }

    public int getAlquileresActivos() {
        return alquileresActivos;
    }

    public int getAlquileresAtrasados() {
        return alquileresAtrasados;
    }

    public int getVehiculosDisponibles() {
        return vehiculosDisponibles;
    }

    public int getVehiculosEnTaller() {
        return vehiculosEnTaller;
    }

    public int getVehiculosVendidos() {
        return vehiculosVendidos;
    }

    public int getUsuariosActivos() {
        return usuariosActivos;
    }
}