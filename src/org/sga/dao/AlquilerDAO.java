package org.sga.dao;

import org.sga.model.Alquiler;

public interface AlquilerDAO extends CRUD<Alquiler, Integer> {

    boolean registrarAlquiler(Alquiler alquiler);
}