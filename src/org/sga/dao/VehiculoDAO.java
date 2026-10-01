package org.sga.dao;

import org.sga.model.Vehiculo;

public interface VehiculoDAO extends CRUD<Vehiculo, Integer> {
    Vehiculo buscarPorPlaca(String placa);
}