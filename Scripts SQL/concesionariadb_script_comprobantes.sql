use concesionariadb_in4cm;

drop procedure if exists sp_guardarcliente;
drop procedure if exists sp_listaralquileresporasesor;

delimiter $$

-- guarda al cliente; si el CUI ya existe, actualiza sus datos
create procedure sp_guardarcliente(
    in _cui bigint,
    in _nombres varchar(100),
    in _apellidos varchar(100),
    in _telefono varchar(15),
    in _correo varchar(100),
    in _licencia varchar(30)
)
begin
    insert into clientes(cui, nombres, apellidos, telefono, correo, licencia)
    values (_cui, _nombres, _apellidos, _telefono, _correo, _licencia)
    on duplicate key update
        nombres = _nombres,
        apellidos = _apellidos,
        telefono = _telefono,
        correo = ifnull(_correo, correo),
        licencia = _licencia;
end $$

-- alquileres de un asesor con placa, descripción del vehículo y nombre del cliente
create procedure sp_listaralquileresporasesor(
    in _id_asesor int
)
begin
    select al.id_alquiler, al.id_vehiculo, al.cui_cliente, al.id_asesor, al.fecha_salida, al.fecha_regreso,
           al.lleva_seguro, al.fecha_devolucion_real, al.fecha_registro,
           veh.placa,
           concat(veh.marca, ' ', veh.modelo, ' ', veh.anio),
           concat(c.nombres, ' ', c.apellidos)
    from alquileres al
    inner join vehiculos veh on al.id_vehiculo = veh.id_vehiculo
    inner join clientes c on al.cui_cliente = c.cui
    where al.id_asesor = _id_asesor
    order by al.fecha_registro desc, al.id_alquiler desc;
end $$

delimiter ;