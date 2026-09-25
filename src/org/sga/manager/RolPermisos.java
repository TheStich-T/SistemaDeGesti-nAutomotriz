package org.sga.manager;

public class RolPermisos {

    private RolPermisos() {
    }

    public static String getDashboardPorRol(String rol) {
        if (rol == null) {
            return null;
        }
        return switch (rol.toLowerCase()) {
            case "admin" ->
                "/org/sga/view/AdminDashboardView.fxml";
            case "provisionador" ->
                "/org/sga/view/ProvisionadorDashboardView.fxml";
            case "mecanico" ->
                "/org/sga/view/MecanicoDashboardView.fxml";
            case "asesor" ->
                "/org/sga/view/AsesorDashboardView.fxml";
            default ->
                null;
        };
    }
}
