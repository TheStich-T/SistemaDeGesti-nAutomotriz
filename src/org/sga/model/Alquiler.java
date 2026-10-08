package org.sga.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Alquiler {

    private int id;
    private int idVehiculo;
    private long cuiCliente;
    private int idAsesor;
    private LocalDate fechaSalida;
    private LocalDate fechaRegreso;
    private boolean llevaSeguro;
    private LocalDate fechaDevolucionReal;
    private LocalDateTime fechaRegistro;

}

