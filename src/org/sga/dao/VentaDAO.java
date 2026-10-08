package org.sga.dao;

import java.util.List;
import org.sga.model.Venta;

public interface VentaDAO extends CRUD<Venta, Integer> {

    boolean registrarVenta(Venta venta);
    List<Venta> listarPorAsesor(int idAsesor);
}