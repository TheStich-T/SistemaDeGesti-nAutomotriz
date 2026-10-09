package org.sga.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sga.dao.ReporteDAO;
import org.sga.model.FilaReporte;
import org.sga.model.Indicadores;
import org.sga.util.Conexion;

public class ReporteDAOImpl implements ReporteDAO {

    private static final Logger log = Logger.getLogger(ReporteDAOImpl.class.getName());

    // devuelve null si no se pudo consultar
    @Override
    public Indicadores obtenerIndicadores() {
        log.info("Consultando indicadores generales");
        String sql = "{call sp_indicadoresgenerales()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            if (tablaResultado.next()) {
                return new Indicadores(
                        tablaResultado.getInt(1),
                        tablaResultado.getBigDecimal(2),
                        tablaResultado.getInt(3),
                        tablaResultado.getInt(4),
                        tablaResultado.getInt(5),
                        tablaResultado.getInt(6),
                        tablaResultado.getInt(7),
                        tablaResultado.getInt(8));
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al consultar los indicadores generales", e);
        }
        return null;
    }

    @Override
    public List<FilaReporte> reporteVentas(LocalDate desde, LocalDate hasta) {
        log.info("Reporte de ventas del " + desde + " al " + hasta);
        List<FilaReporte> filas = new ArrayList<>();
        String sql = "{call sp_reporteventasporperiodo(?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setDate(1, Date.valueOf(desde));
            consulta.setDate(2, Date.valueOf(hasta));
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    filas.add(mapearFila(tablaResultado));
                }
            }
            log.info("Ventas en el período: " + filas.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al generar el reporte de ventas", e);
        }
        return filas;
    }

    @Override
    public List<FilaReporte> reporteAlquileres(LocalDate desde, LocalDate hasta) {
        log.info("Reporte de alquileres del " + desde + " al " + hasta);
        List<FilaReporte> filas = new ArrayList<>();
        String sql = "{call sp_reportealquileresporperiodo(?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setDate(1, Date.valueOf(desde));
            consulta.setDate(2, Date.valueOf(hasta));
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    FilaReporte fila = mapearFila(tablaResultado);
                    // el SP de alquileres agrega: fecha de regreso pactada y estado
                    Date regreso = tablaResultado.getDate(8);
                    if (regreso != null) {
                        fila.setFechaFin(regreso.toLocalDate());
                    }
                    fila.setEstado(tablaResultado.getString(9));
                    filas.add(fila);
                }
            }
            log.info("Alquileres en el período: " + filas.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al generar el reporte de alquileres", e);
        }
        return filas;
    }

    // columnas comunes de ambos reportes: id, placa, vehículo, cliente, asesor, fecha y monto
    private FilaReporte mapearFila(ResultSet tablaResultado) throws SQLException {
        FilaReporte fila = new FilaReporte();
        fila.setId(tablaResultado.getInt(1));
        fila.setPlaca(tablaResultado.getString(2));
        fila.setVehiculo(tablaResultado.getString(3));
        fila.setCliente(tablaResultado.getString(4));
        fila.setAsesor(tablaResultado.getString(5));
        Date fecha = tablaResultado.getDate(6);
        if (fecha != null) {
            fila.setFecha(fecha.toLocalDate());
        }
        fila.setMonto(tablaResultado.getBigDecimal(7));
        return fila;
    }
}