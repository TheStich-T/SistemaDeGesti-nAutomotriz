package org.sga.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sga.dao.UsuarioDAO;
import org.sga.model.Usuario;
import org.sga.util.Conexion;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final Logger log = Logger.getLogger(UsuarioDAOImpl.class.getName());
    
    @Override
    public Usuario iniciarSesion(String username, String passwordHash) {
        log.info("Intentando iniciar sesión para usuario: " + username);
        Usuario usuario = null;
        String sql = "{call sp_iniciar_sesion(?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consultaCall = conexion.prepareCall(sql)) {
            consultaCall.setString(1, username);
            consultaCall.setString(2, passwordHash);
            try (ResultSet tablaResultado = consultaCall.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt(1));
                    usuario.setUsername(tablaResultado.getString(2));
                    usuario.setRol(tablaResultado.getString(3));
                    usuario.setActivo(true);
                    log.info("Inicio de sesión exitoso para usuario: " + username);
                } else {
                    log.warning("Inicio de sesión fallido para usuario: " + username);
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al iniciar sesión para usuario: " + username, e);
        }
        return usuario;
    }

    @Override
    public boolean insertar(Usuario objeto) {
        log.info("Registrando usuario: " + objeto.getUsername() + ", rol: " + objeto.getRol());
        String sql = "{call sp_insertarusuario(?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setString(1, objeto.getUsername());
            consulta.setString(2, objeto.getPasswordHash());
            consulta.setString(3, objeto.getRol());
            int filasAfectadas = consulta.executeUpdate();
            boolean registrado = filasAfectadas > 0;
            if (registrado) {
                log.info("Usuario registrado: " + objeto.getUsername());
            }
            return registrado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al registrar usuario: " + objeto.getUsername(), e);
            return false;
        }
    }

    @Override
    public List<Usuario> listar() {
        log.info("Listando usuarios");
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "{call sp_listarusuarios()}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql);
             ResultSet tablaResultado = consulta.executeQuery()) {
            while (tablaResultado.next()) {
                Usuario usuario = new Usuario();
                // El SP devuelve: id_usuario, username, rol, activo, fecha_creacion
                usuario.setId(tablaResultado.getInt(1));
                usuario.setUsername(tablaResultado.getString(2));
                usuario.setRol(tablaResultado.getString(3));
                usuario.setActivo(tablaResultado.getBoolean(4));
                Timestamp fecha = tablaResultado.getTimestamp(5);
                if (fecha != null) {
                    usuario.setFechaCreacion(fecha.toLocalDateTime());
                }
                usuarios.add(usuario);
            }
            log.info("Usuarios listados correctamente: " + usuarios.size());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al listar usuarios", e);
        }
        return usuarios;
    }

    @Override
    public Usuario buscar(Integer id) {
        log.info("Buscando usuario por ID: " + id);
        Usuario usuario = null;
        String sql = "{call sp_buscarusuario(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            try (ResultSet tablaResultado = consulta.executeQuery()) {
                if (tablaResultado.next()) {
                    usuario = new Usuario();
                    usuario.setId(tablaResultado.getInt(1));
                    usuario.setUsername(tablaResultado.getString(2));
                    usuario.setRol(tablaResultado.getString(3));
                    usuario.setActivo(tablaResultado.getBoolean(4));
                    Timestamp fecha = tablaResultado.getTimestamp(5);
                    if (fecha != null) {
                        usuario.setFechaCreacion(fecha.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al buscar usuario por ID: " + id, e);
        }
        return usuario;
    }

    @Override
    public boolean actualizar(Usuario objeto) {
        log.info("Actualizando usuario: " + objeto.getUsername());
        String sql = "{call sp_actualizarusuario(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, objeto.getId());
            consulta.setString(2, objeto.getUsername());
            consulta.setString(3, objeto.getRol());
            consulta.setBoolean(4, objeto.isActivo());
            int filasAfectadas = consulta.executeUpdate();
            boolean actualizado = filasAfectadas > 0;
            if (actualizado) {
                log.info("Usuario actualizado: " + objeto.getUsername());
            }
            return actualizado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al actualizar usuario: " + objeto.getUsername(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        log.info("Eliminando usuario: " + id);
        String sql = "{call sp_eliminarusuario(?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, id);
            int filasAfectadas = consulta.executeUpdate();
            boolean eliminado = filasAfectadas > 0;
            if (eliminado) {
                log.info("Usuario eliminado: " + id);
            }
            return eliminado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al eliminar usuario: " + id, e);
            return false;
        }
    }
    @Override
    public boolean desactivar(int id) {
        log.info("Desactivando usuario: " + id);
        Usuario usuario = buscar(id);
        if (usuario == null) {
            log.warning("No se encontró el usuario a desactivar: " + id);
            return false;
        }
        String sql = "{call sp_actualizarusuario(?, ?, ?, ?)}";
        try (Connection conexion = Conexion.getInstancia().conectar();
             CallableStatement consulta = conexion.prepareCall(sql)) {
            consulta.setInt(1, usuario.getId());
            consulta.setString(2, usuario.getUsername());
            consulta.setString(3, usuario.getRol());
            consulta.setBoolean(4, false);
            int filasAfectadas = consulta.executeUpdate();
            boolean desactivado = filasAfectadas > 0;
            if (desactivado) {
                log.info("Usuario desactivado: " + id);
            }
            return desactivado;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Error al desactivar usuario: " + id, e);
            return false;
        }
    }
}