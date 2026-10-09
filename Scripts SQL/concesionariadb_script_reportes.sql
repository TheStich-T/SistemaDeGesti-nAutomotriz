use concesionariadb_in4cm;

drop procedure if exists sp_indicadoresgenerales;
drop procedure if exists sp_reporteventasporperiodo;
drop procedure if exists sp_reportealquileresporperiodo;

delimiter $$

-- indicadores del dashboard del administrador, devuelve una sola fila con 8 columnas: ventas de hoy, monto vendido en el mes, alquileres activos, alquileres atrasados, vehículos disponibles, vehículos en taller, vehículos vendidos y usuarios activos
-- un alquiler está activo si aún no se devuelve y está atrasado si, además, ya pasó su fecha de regreso
create procedure sp_indicadoresgenerales()
begin
    select
        (select count(*) from ventas
          where date(fecha_venta) = curdate()),
        (select ifnull(sum(precio), 0) from ventas
          where year(fecha_venta) = year(curdate()) and month(fecha_venta) = month(curdate())),
        (select count(*) from alquileres
          where fecha_devolucion_real is null),
        (select count(*) from alquileres
          where fecha_devolucion_real is null and fecha_regreso < curdate()),
        (select count(*) from vehiculos where estado = 'disponible'),
        (select count(*) from vehiculos where estado = 'en_taller'),
        (select count(*) from vehiculos where estado = 'vendido'),
        (select count(*) from usuarios where activo = true);
end $$

-- ventas realizadas entre dos fechas (ambas incluidas), de todos los asesores
create procedure sp_reporteventasporperiodo(
    in _desde date,
    in _hasta date
)
begin
    select ve.id_venta,
           veh.placa,
           concat(veh.marca, ' ', veh.modelo, ' ', veh.anio),
           concat(c.nombres, ' ', c.apellidos),
           u.username,
           date(ve.fecha_venta),
           ve.precio
    from ventas ve
    inner join vehiculos veh on ve.id_vehiculo = veh.id_vehiculo
    inner join clientes c on ve.cui_cliente = c.cui
    inner join usuarios u on ve.id_asesor = u.id_usuario
    where ve.fecha_venta >= _desde
      and ve.fecha_venta < date_add(_hasta, interval 1 day)
    order by ve.fecha_venta desc, ve.id_venta desc;
end $$

-- alquileres cuya fecha de salida cae entre dos fechas (ambas incluidas), de todos los asesores, el estado se calcula con la fecha de devolución real y la fecha de regreso pactada
create procedure sp_reportealquileresporperiodo(
    in _desde date,
    in _hasta date
)
begin
    select al.id_alquiler,
           veh.placa,
           concat(veh.marca, ' ', veh.modelo, ' ', veh.anio),
           concat(c.nombres, ' ', c.apellidos),
           u.username,
           al.fecha_salida,
           al.total,
           al.fecha_regreso,
           case
               when al.fecha_devolucion_real is null and al.fecha_regreso < curdate() then 'Atrasado'
               when al.fecha_devolucion_real is null then 'Activo'
               when al.fecha_devolucion_real > al.fecha_regreso then 'Devuelto con atraso'
               else 'Devuelto'
           end
    from alquileres al
    inner join vehiculos veh on al.id_vehiculo = veh.id_vehiculo
    inner join clientes c on al.cui_cliente = c.cui
    inner join usuarios u on al.id_asesor = u.id_usuario
    where al.fecha_salida between _desde and _hasta
    order by al.fecha_salida desc, al.id_alquiler desc;
end $$

delimiter ;