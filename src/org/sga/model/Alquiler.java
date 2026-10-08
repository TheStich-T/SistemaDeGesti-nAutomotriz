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

    public Alquiler() {
    }

    public Alquiler(int id, int idVehiculo, long cuiCliente, int idAsesor,
            LocalDate fechaSalida, LocalDate fechaRegreso, boolean llevaSeguro,
            LocalDate fechaDevolucionReal, LocalDateTime fechaRegistro) {
        this.id = id;
        this.idVehiculo = idVehiculo;
        this.cuiCliente = cuiCliente;
        this.idAsesor = idAsesor;
        this.fechaSalida = fechaSalida;
        this.fechaRegreso = fechaRegreso;
        this.llevaSeguro = llevaSeguro;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.fechaRegistro = fechaRegistro;
    }
}

