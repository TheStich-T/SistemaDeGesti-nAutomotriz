package org.sga.dao;

import java.util.List;
import org.sga.model.Alquiler;

public interface AlquilerDAO extends CRUD<Alquiler, Integer> {
    boolean registrarAlquiler(Alquiler alquiler);
    List<Alquiler> listarPorAsesor(int idAsesor);
    List<Alquiler> listarActivos();
    boolean registrarDevolucion(Alquiler alquiler);
}