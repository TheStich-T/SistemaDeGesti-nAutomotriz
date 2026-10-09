use concesionariadb_in4cm;

drop procedure if exists sp_marcarvehiculoalquilado;
drop procedure if exists sp_registraralquiler;

delimiter $$

-- cambia el vehículo a 'en_alquiler' solo si está disponible y permite alquiler (si no cumple, no afecta ninguna fila y el DAO devuelve false)
create procedure sp_marcarvehiculoalquilado(
    in _id_vehiculo int
)
begin
    update vehiculos
    set estado = 'en_alquiler'
    where id_vehiculo = _id_vehiculo
      and estado = 'disponible'
      and operacion_permitida in ('alquiler', 'ambas');
end $$

-- registra el alquiler: la fecha de salida es hoy y devuelve el id generado
create procedure sp_registraralquiler(
    in _id_vehiculo int,
    in _cui_cliente bigint,
    in _id_asesor int,
    in _fecha_regreso date,
    in _lleva_seguro boolean,
    out _id_alquiler int
)
begin
    insert into alquileres(id_vehiculo, cui_cliente, id_asesor, fecha_salida, fecha_regreso, lleva_seguro)
    values (_id_vehiculo, _cui_cliente, _id_asesor, curdate(), _fecha_regreso, _lleva_seguro);

    set _id_alquiler = last_insert_id();
end $$

delimiter ;