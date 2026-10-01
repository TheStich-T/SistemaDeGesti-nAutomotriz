package org.sga.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sga.dao.VehiculoDAO;
import org.sga.model.Vehiculo;
import org.sga.util.Conexion;

public class VehiculoDAOImpl implements VehiculoDAO {

    private static final Logger log = Logger.getLogger(VehiculoDAOImpl.class.getName());

    @Override
    public boolean insertar(Vehiculo objeto) {
        log.info("Registrando vehículo: " + objeto.getPlaca() + ", estado inicial: " + objeto.getEstado());
        String sql = "{call sp_insertarvehiculo(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getPlaca());
            consulta.setString(2, objeto.getMarca());
            consulta.setString(3, objeto.getModelo());
            consulta.setInt(4, objeto.getAnio());
            consulta.setString(5, objeto.getColor());
            consulta.setString(6, objeto.getCondicion());
            consulta.setString(7, objeto.getProveedor());
            consulta.setBigDecimal(8, objeto.getCosto());
            if (objeto.getObservaciones() != null) {
                consulta.setString(9, objeto.getObservaciones());
            } else {
                consulta.setNull(9, Types.VARCHAR);
            }
            consulta.setString(10, objeto.getEstado());
            consulta.setInt(11, objeto.getIdUsuarioProvisionador());
            int filasAfectadas = consulta.executeUpdate();
            boolean registrado = filasAfectadas > 0;
            if (registrado) {
                log.info("Vehículo registrado: " + objeto.getPlaca());
            }
            return registrado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar vehículo: " + objeto.getPlaca(), e);
            return false;
        }
    }

    @Override
    public List<Vehiculo> listar() {
        log.info("Listando vehículos");
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "{call sp_listarvehiculos()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                vehiculos.add(mapearVehiculo(tablaResultado));
            }
            log.info("Vehículos listados correctamente: " + vehiculos.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar vehículos", e);
        }
        return vehiculos;
    }

    @Override
    public Vehiculo buscar(Integer id) {
        log.info("Buscando vehículo por ID: " + id);
        Vehiculo vehiculo = null;
        String sql = "{call sp_buscarvehiculo(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    vehiculo = mapearVehiculo(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar vehículo por ID: " + id, e);
        }
        return vehiculo;
    }

    @Override
    public Vehiculo buscarPorPlaca(String placa) {
        log.info("Buscando vehículo por placa: " + placa);
        Vehiculo vehiculo = null;
        String sql = "{call sp_buscarvehiculoporplaca(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, placa);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    vehiculo = mapearVehiculo(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar vehículo por placa: " + placa, e);
        }
        return vehiculo;
    }

    @Override
    public boolean actualizar(Vehiculo objeto) {
        log.info("Actualizando vehículo: " + objeto.getPlaca());
        String sql = "{call sp_actualizarvehiculo(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getId());
            consulta.setString(2, objeto.getPlaca());
            consulta.setString(3, objeto.getMarca());
            consulta.setString(4, objeto.getModelo());
            consulta.setInt(5, objeto.getAnio());
            consulta.setString(6, objeto.getColor());
            consulta.setString(7, objeto.getCondicion());
            consulta.setString(8, objeto.getProveedor());
            consulta.setBigDecimal(9, objeto.getCosto());
            if (objeto.getObservaciones() != null) {
                consulta.setString(10, objeto.getObservaciones());
            } else {
                consulta.setNull(10, Types.VARCHAR);
            }
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) {
                log.info("Vehículo actualizado: " + objeto.getPlaca());
            }
            return actualizado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar vehículo: " + objeto.getPlaca(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        log.info("Eliminando vehículo: " + id);
        String sql = "{call sp_eliminarvehiculo(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) {
                log.info("Vehículo eliminado: " + id);
            }
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar vehículo: " + id, e);
            return false;
        }
    }
    
    private Vehiculo mapearVehiculo(ResultSet tablaResultado) throws SQLException {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(tablaResultado.getInt(1));
        vehiculo.setPlaca(tablaResultado.getString(2));
        vehiculo.setMarca(tablaResultado.getString(3));
        vehiculo.setModelo(tablaResultado.getString(4));
        vehiculo.setAnio(tablaResultado.getInt(5));
        vehiculo.setColor(tablaResultado.getString(6));
        vehiculo.setCondicion(tablaResultado.getString(7));
        vehiculo.setProveedor(tablaResultado.getString(8));
        vehiculo.setCosto(tablaResultado.getBigDecimal(9));
        vehiculo.setObservaciones(tablaResultado.getString(10));
        vehiculo.setEstado(tablaResultado.getString(11));
        vehiculo.setProgresoTaller(tablaResultado.getString(12));
        vehiculo.setIdUsuarioProvisionador(tablaResultado.getInt(13));
        Timestamp fecha = tablaResultado.getTimestamp(14);
        if (fecha != null) {
            vehiculo.setFechaIngreso(fecha.toLocalDateTime());
        }
        return vehiculo;
    }
}