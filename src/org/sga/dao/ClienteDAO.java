package org.sga.dao;

import org.sga.model.Cliente;

public interface ClienteDAO extends CRUD<Cliente, Long> {
    boolean guardar(Cliente cliente);
}