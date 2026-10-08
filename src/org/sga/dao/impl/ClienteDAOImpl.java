package org.sga.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sga.dao.ClienteDAO;
import org.sga.model.Cliente;
import org.sga.util.Conexion;

public class ClienteDAOImpl implements ClienteDAO {

    private static final Logger log = Logger.getLogger(ClienteDAOImpl.class.getName());

    @Override
    public boolean insertar(Cliente objeto) {
        log.info("Registrando cliente: " + objeto.getCui());
        String sql = "{call sp_insertarcliente(?, ?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setLong(1, objeto.getCui());
            consulta.setString(2, objeto.getNombres());
            consulta.setString(3, objeto.getApellidos());
            consulta.setString(4, objeto.getTelefono());
            consulta.setString(5, objeto.getCorreo());
            consulta.setString(6, objeto.getLicencia());
            int filasAfectadas = consulta.executeUpdate();
            boolean registrado = filasAfectadas > 0;
            if (registrado) {
                log.info("Cliente registrado: " + objeto.getCui());
            }
            return registrado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar cliente: " + objeto.getCui(), e);
            return false;
        }
    }

    @Override
    public List<Cliente> listar() {
        log.info("Listando clientes");
        List<Cliente> clientes = new ArrayList<>();
        String sql = "{call sp_listarclientes()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                clientes.add(mapearCliente(tablaResultado));
            }
            log.info("Clientes listados correctamente: " + clientes.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar clientes", e);
        }
        return clientes;
    }

    @Override
    public Cliente buscar(Long cui) {
        log.info("Buscando cliente por CUI: " + cui);
        Cliente cliente = null;
        String sql = "{call sp_buscarcliente(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setLong(1, cui);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    cliente = mapearCliente(tablaResultado);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar cliente por CUI: " + cui, e);
        }
        return cliente;
    }

    @Override
    public boolean actualizar(Cliente objeto) {
        log.info("Actualizando cliente: " + objeto.getCui());
        String sql = "{call sp_actualizarcliente(?, ?, ?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setLong(1, objeto.getCui());
            consulta.setString(2, objeto.getNombres());
            consulta.setString(3, objeto.getApellidos());
            consulta.setString(4, objeto.getTelefono());
            consulta.setString(5, objeto.getCorreo());
            consulta.setString(6, objeto.getLicencia());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) {
                log.info("Cliente actualizado: " + objeto.getCui());
            }
            return actualizado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar cliente: " + objeto.getCui(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Long cui) {
        log.info("Eliminando cliente: " + cui);
        String sql = "{call sp_eliminarcliente(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setLong(1, cui);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) {
                log.info("Cliente eliminado: " + cui);
            }
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar cliente: " + cui, e);
            return false;
        }
    }

    private Cliente mapearCliente(ResultSet tablaResultado) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setCui(tablaResultado.getLong(1));
        cliente.setNombres(tablaResultado.getString(2));
        cliente.setApellidos(tablaResultado.getString(3));
        cliente.setTelefono(tablaResultado.getString(4));
        cliente.setCorreo(tablaResultado.getString(5));
        cliente.setLicencia(tablaResultado.getString(6));
        return cliente;
    }
}