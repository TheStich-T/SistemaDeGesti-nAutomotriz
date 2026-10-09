package org.sga.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum PeriodoReporte {

    DIA("Día"),
    SEMANA("Semana (lunes a domingo)"),
    MES("Mes");

    private final String etiqueta;

    PeriodoReporte(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public LocalDate desde(LocalDate referencia) {
        return switch (this) {
            case DIA ->
                referencia;
            case SEMANA ->
                referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES ->
                referencia.withDayOfMonth(1);
        };
    }

    public LocalDate hasta(LocalDate referencia) {
        return switch (this) {
            case DIA ->
                referencia;
            case SEMANA ->
                referencia.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case MES ->
                referencia.with(TemporalAdjusters.lastDayOfMonth());
        };
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}