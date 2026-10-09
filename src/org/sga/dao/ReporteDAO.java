package org.sga.dao;

import java.time.LocalDate;
import java.util.List;
import org.sga.model.FilaReporte;
import org.sga.model.Indicadores;

public interface ReporteDAO {

    Indicadores obtenerIndicadores();
    List<FilaReporte> reporteVentas(LocalDate desde, LocalDate hasta);
    List<FilaReporte> reporteAlquileres(LocalDate desde, LocalDate hasta);
}