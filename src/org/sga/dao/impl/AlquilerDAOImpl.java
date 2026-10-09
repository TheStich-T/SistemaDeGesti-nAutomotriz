package org.sga.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sga.dao.AlquilerDAO;
import org.sga.model.Alquiler;
import org.sga.util.Conexion;

public class AlquilerDAOImpl implements AlquilerDAO {

    private static final Logger log = Logger.getLogger(AlquilerDAOImpl.class.getName());

    @Override
    public boolean insertar(Alquiler objeto) {
        log.info("Registrando alquiler del vehículo: " + objeto.getIdVehiculo());
        String sql = "{call sp_insertaralquiler(?, ?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getIdVehiculo());
            consulta.setLong(2, objeto.getCuiCliente());
            consulta.setInt(3, objeto.getIdAsesor());
            consulta.setDate(4, Date.valueOf(objeto.getFechaSalida()));
            consulta.setDate(5, Date.valueOf(objeto.getFechaRegreso()));
            consulta.setBoolean(6, objeto.isLlevaSeguro());
            int filasAfectadas = consulta.executeUpdate();
            boolean registrado = filasAfectadas > 0;
            if (registrado) {
                log.info("Alquiler registrado del vehículo: " + objeto.getIdVehiculo());
            }
            return registrado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar alquiler del vehículo: " + objeto.getIdVehiculo(), e);
            return false;
        }
    }

    // alquiler + cambio de estado del vehículo en una sola transacción
    @Override
    public boolean registrarAlquiler(Alquiler alquiler) {
        log.info("Registrando alquiler con transacción. Vehículo: " + alquiler.getIdVehiculo()
                + ", asesor: " + alquiler.getIdAsesor());
        Connection conexion = null;
        try {
            conexion = Conexion.getInstancia().conectar();
            conexion.setAutoCommit(false);

            try (CallableStatement marcarAlquilado = conexion.prepareCall("{call sp_marcarvehiculoalquilado(?)}");
                 CallableStatement insertarAlquiler = conexion.prepareCall("{call sp_registraralquiler(?, ?, ?, ?, ?, ?)}")) {

                marcarAlquilado.setInt(1, alquiler.getIdVehiculo());
                if (marcarAlquilado.executeUpdate() == 0) {
                    conexion.rollback();
                    log.warning("El vehículo " + alquiler.getIdVehiculo() + " no está disponible para alquiler");
                    return false;
                }

                insertarAlquiler.setInt(1, alquiler.getIdVehiculo());
                insertarAlquiler.setLong(2, alquiler.getCuiCliente());
                insertarAlquiler.setInt(3, alquiler.getIdAsesor());
                insertarAlquiler.setDate(4, Date.valueOf(alquiler.getFechaRegreso()));
                insertarAlquiler.setBoolean(5, alquiler.isLlevaSeguro());
                insertarAlquiler.registerOutParameter(6, Types.INTEGER);
                insertarAlquiler.execute();
                alquiler.setId(insertarAlquiler.getInt(6));

                conexion.commit();
                log.info("Alquiler registrado y vehículo marcado como en alquiler: " + alquiler.getIdVehiculo());
                return true;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar el alquiler del vehículo: " + alquiler.getIdVehiculo(), e);
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    log.log(Level.SEVERE, "Error al revertir la transacción de alquiler", ex);
                }
            }
            return false;
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException e) {
                    log.log(Level.WARNING, "Error al cerrar la conexión de alquiler", e);
                }
            }
        }
    }

    @Override
    public List<Alquiler> listar() {
        log.info("Listando alquileres");
        List<Alquiler> alquileres = new ArrayList<>();
        String sql = "{call sp_listaralquileres()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                alquileres.add(mapearAlquiler(tablaResultado));
            }
            log.info("Alquileres listados correctamente: " + alquileres.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar alquileres", e);
        }
        return alquileres;
    }

    @Override
    public List<Alquiler> listarPorAsesor(int idAsesor) {
        log.info("Listando alquileres del asesor: " + idAsesor);
        List<Alquiler> alquileres = new ArrayList<>();
        String sql = "{call sp_listaralquileresporasesor(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, idAsesor);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                while (tablaResultado.next()) {
                    Alquiler alquiler = mapearAlquiler(tablaResultado);
                    // el SP agrega: placa, descripción del vehículo y nombre del cliente
                    alquiler.setPlaca(tablaResultado.getString(10));
                    alquiler.setDescripcionVehiculo(tablaResultado.getString(11));
                    alquiler.setNombreCliente(tablaResultado.getString(12));
                    alquileres.add(alquiler);
                }
            }
            log.info("Alquileres del asesor " + idAsesor + ": " + alquileres.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar alquileres del asesor: " + idAsesor, e);
        }
        return alquileres;
    }

    @Override
    public Alquiler buscar(Integer id) {
        log.info("Buscando alquiler por ID: " + id);
        Alquiler alquiler = null;
        String sql = "{call sp_buscaralquiler(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    alquiler = mapearAlquiler(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar alquiler por ID: " + id, e);
        }
        return alquiler;
    }

    @Override
    public boolean actualizar(Alquiler objeto) {
        log.info("Actualizando alquiler: " + objeto.getId());
        String sql = "{call sp_actualizaralquiler(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getId());
            consulta.setDate(2, Date.valueOf(objeto.getFechaSalida()));
            consulta.setDate(3, Date.valueOf(objeto.getFechaRegreso()));
            consulta.setBoolean(4, objeto.isLlevaSeguro());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) {
                log.info("Alquiler actualizado: " + objeto.getId());
            }
            return actualizado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar alquiler: " + objeto.getId(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        log.info("Eliminando alquiler: " + id);
        String sql = "{call sp_eliminaralquiler(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) {
                log.info("Alquiler eliminado: " + id);
            }
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar alquiler: " + id, e);
            return false;
        }
    }

    private Alquiler mapearAlquiler(ResultSet tablaResultado) throws SQLException {
        Alquiler alquiler = new Alquiler();
        alquiler.setId(tablaResultado.getInt(1));
        alquiler.setIdVehiculo(tablaResultado.getInt(2));
        alquiler.setCuiCliente(tablaResultado.getLong(3));
        alquiler.setIdAsesor(tablaResultado.getInt(4));
        Date salida = tablaResultado.getDate(5);
        if (salida != null) {
            alquiler.setFechaSalida(salida.toLocalDate());
        }
        Date regreso = tablaResultado.getDate(6);
        if (regreso != null) {
            alquiler.setFechaRegreso(regreso.toLocalDate());
        }
        alquiler.setLlevaSeguro(tablaResultado.getBoolean(7));
        Date devolucion = tablaResultado.getDate(8);
        if (devolucion != null) {
            alquiler.setFechaDevolucionReal(devolucion.toLocalDate());
        }
        Timestamp registro = tablaResultado.getTimestamp(9);
        if (registro != null) {
            alquiler.setFechaRegistro(registro.toLocalDateTime());
        }
        return alquiler;
    }
}