package org.sga.dao;

import java.util.List;
import org.sga.model.Vehiculo;

public interface VehiculoDAO extends CRUD<Vehiculo, Integer> {

    Vehiculo buscarPorPlaca(String placa);
    List<Vehiculo> listarColaTaller();
    boolean actualizarProgresoTaller(int idVehiculo, String progresoTaller);
}

