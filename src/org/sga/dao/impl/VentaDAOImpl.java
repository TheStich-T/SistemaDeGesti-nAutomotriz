package org.sga.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sga.dao.VentaDAO;
import org.sga.model.Venta;
import org.sga.util.Conexion;

public class VentaDAOImpl implements VentaDAO {

    private static final Logger log = Logger.getLogger(VentaDAOImpl.class.getName());

    @Override
    public boolean insertar(Venta objeto) {
        log.info("Registrando venta del vehículo: " + objeto.getIdVehiculo());
        String sql = "{call sp_insertarventa(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getIdVehiculo());
            consulta.setLong(2, objeto.getCuiCliente());
            consulta.setInt(3, objeto.getIdAsesor());
            consulta.setBigDecimal(4, objeto.getPrecio());
            int filasAfectadas = consulta.executeUpdate();
            boolean registrada = filasAfectadas > 0;
            if (registrada) {
                log.info("Venta registrada del vehículo: " + objeto.getIdVehiculo());
            }
            return registrada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar venta del vehículo: " + objeto.getIdVehiculo(), e);
            return false;
        }
    }

    @Override
    public boolean registrarVenta(Venta venta) {
        log.info("Registrando venta con transacción. Vehículo: " + venta.getIdVehiculo()
                + ", asesor: " + venta.getIdAsesor());
        Connection conexion = null;
        try {
            conexion = Conexion.getInstancia().conectar();
            conexion.setAutoCommit(false);

            try (CallableStatement marcarVendido = conexion.prepareCall("{call sp_marcarvehiculovendido(?)}");
                 CallableStatement insertarVenta = conexion.prepareCall("{call sp_insertarventa(?, ?, ?, ?)}")) {

                marcarVendido.setInt(1, venta.getIdVehiculo());
                if (marcarVendido.executeUpdate() == 0) {
                    conexion.rollback();
                    log.warning("El vehículo " + venta.getIdVehiculo() + " no está disponible para la venta");
                    return false;
                }

                insertarVenta.setInt(1, venta.getIdVehiculo());
                insertarVenta.setLong(2, venta.getCuiCliente());
                insertarVenta.setInt(3, venta.getIdAsesor());
                insertarVenta.setBigDecimal(4, venta.getPrecio());
                insertarVenta.executeUpdate();

                // id generado, para mostrar el número de factura
                try (Statement consultaId = conexion.createStatement();
                     ResultSet resultadoId = consultaId.executeQuery("select last_insert_id()")) {
                    if (resultadoId.next()) {
                        venta.setId(resultadoId.getInt(1));
                    }
                }

                conexion.commit();
                log.info("Venta registrada y vehículo marcado como Vendido: " + venta.getIdVehiculo());
                return true;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar la venta del vehículo: " + venta.getIdVehiculo(), e);
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    log.log(Level.SEVERE, "Error al revertir la transacción de venta", ex);
                }
            }
            return false;
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Error al cerrar la conexión de venta", e);
                }
            }
        }
    }

    @Override
    public List<Venta> listar() {
        log.info("Listando ventas");
        List<Venta> ventas = new ArrayList<>();
        String sql = "{call sp_listarventas()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                ventas.add(mapearVenta(tablaResultado));
            }
            log.info("Ventas listadas correctamente: " + ventas.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar ventas", e);
        }
        return ventas;
    }

    @Override
    public List<Venta> listarPorAsesor(int idAsesor) {
        log.info("Listando ventas del asesor: " + idAsesor);
        List<Venta> ventas = new ArrayList<>();
        String sql = "{call sp_listarventasporasesor(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idAsesor);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    Venta venta = mapearVenta(tablaResultado);
                    // el SP agrega: placa, descripción del vehículo y nombre del cliente
                    venta.setPlaca(tablaResultado.getString(7));
                    venta.setDescripcionVehiculo(tablaResultado.getString(8));
                    venta.setNombreCliente(tablaResultado.getString(9));
                    ventas.add(venta);
                }
            }
            log.info("Ventas del asesor " + idAsesor + ": " + ventas.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar ventas del asesor: " + idAsesor, e);
        }
        return ventas;
    }

    @Override
    public Venta buscar(Integer id) {
        log.info("Buscando venta por ID: " + id);
        Venta venta = null;
        String sql = "{call sp_buscarventa(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    venta = mapearVenta(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar venta por ID: " + id, e);
        }
        return venta;
    }

    @Override
    public boolean actualizar(Venta objeto) {
        log.info("Actualizando venta: " + objeto.getId());
        String sql = "{call sp_actualizarventa(?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getId());
            consulta.setBigDecimal(2, objeto.getPrecio());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizada = filasAfectadas > 0;
            if (actualizada) {
                log.info("Venta actualizada: " + objeto.getId());
            }
            return actualizada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar venta: " + objeto.getId(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        log.info("Eliminando venta: " + id);
        String sql = "{call sp_eliminarventa(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminada = filasAfectadas > 0;
            if (eliminada) {
                log.info("Venta eliminada: " + id);
            }
            return eliminada;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar venta: " + id, e);
            return false;
        }
    }

    private Venta mapearVenta(ResultSet tablaResultado) throws SQLException {
        Venta venta = new Venta();
        venta.setId(tablaResultado.getInt(1));
        venta.setIdVehiculo(tablaResultado.getInt(2));
        venta.setCuiCliente(tablaResultado.getLong(3));
        venta.setIdAsesor(tablaResultado.getInt(4));
        venta.setPrecio(tablaResultado.getBigDecimal(5));
        Timestamp fecha = tablaResultado.getTimestamp(6);
        if (fecha != null) {
            venta.setFechaVenta(fecha.toLocalDateTime());
        }
        return venta;
    }
}