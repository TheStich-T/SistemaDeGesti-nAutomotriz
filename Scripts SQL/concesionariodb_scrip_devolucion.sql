use concesionariadb_in4cm;

-- la fecha y hora de la devolución, el estado (devuelto / atrasado) y el cobro adicional los calcula y guarda la base de datos
-- la fecha_devolucion_real (date) se sigue llenando para no afectar sp_indicadoresgenerales ni los reportes por período

drop procedure if exists sp_tmp_columnas_devolucion;
drop function if exists fn_dias_atraso;
drop procedure if exists sp_listaralquileresactivos;
drop procedure if exists sp_registrardevolucion;
drop procedure if exists sp_marcarvehiculodisponible;

delimiter $$

-- agrega las columnas nuevas solo si todavía no existen (el script se puede ejecutar más de una vez)
create procedure sp_tmp_columnas_devolucion()
begin
    if not exists (select 1 from information_schema.columns
                   where table_schema = database()
                     and table_name = 'alquileres'
                     and column_name = 'fecha_hora_devolucion') then
        alter table alquileres
            add column fecha_hora_devolucion datetime null,
            add column estado_devolucion enum('devuelto', 'atrasado') null,
            add column dias_atraso int not null default 0,
            add column cobro_adicional decimal(10,2) not null default 0;
    end if;
end $$

delimiter ;

call sp_tmp_columnas_devolucion();
drop procedure sp_tmp_columnas_devolucion;

delimiter $$

create function fn_dias_atraso(_fecha_regreso date)
returns int
reads sql data
begin
    return greatest(datediff(curdate(), _fecha_regreso), 0);
end $$

-- columnas 1 a 12 iguales a sp_listaralquileresporasesor, 13 tarifa por día, 14 días de atraso y 15 cobro adicional estimado a hoy
create procedure sp_listaralquileresactivos()
begin
    select al.id_alquiler, al.id_vehiculo, al.cui_cliente, al.id_asesor, al.fecha_salida, al.fecha_regreso,
           al.lleva_seguro, al.fecha_devolucion_real, al.fecha_registro,
           veh.placa,
           concat(veh.marca, ' ', veh.modelo, ' ', veh.anio),
           concat(c.nombres, ' ', c.apellidos),
           al.precio_dia,
           fn_dias_atraso(al.fecha_regreso),
           fn_dias_atraso(al.fecha_regreso) * al.precio_dia
    from alquileres al
    inner join vehiculos veh on al.id_vehiculo = veh.id_vehiculo
    inner join clientes c on al.cui_cliente = c.cui
    where al.fecha_devolucion_real is null
    order by al.fecha_regreso asc, al.id_alquiler asc;
end $$

-- solo afecta alquileres que no se han devuelto: si no cumple, todos los parámetros de salida quedan en NULL y el DAO devuelve false
create procedure sp_registrardevolucion(
    in _id_alquiler int,
    out _estado varchar(10),
    out _dias_atraso int,
    out _cobro_adicional decimal(10,2),
    out _fecha_hora_devolucion datetime
)
begin
    declare _regreso date;
    declare _precio decimal(10,2);
    declare _dias int;
    declare _ahora datetime;

    set _estado = null;
    set _dias_atraso = null;
    set _cobro_adicional = null;
    set _fecha_hora_devolucion = null;

    -- max() para que siempre devuelva una fila (NULL si el alquiler no existe o ya se devolvió)
    select max(fecha_regreso), max(precio_dia) into _regreso, _precio
    from alquileres
    where id_alquiler = _id_alquiler
      and fecha_devolucion_real is null;

    if _regreso is not null then
        set _dias = fn_dias_atraso(_regreso);
        set _ahora = now();

        update alquileres
        set fecha_devolucion_real = date(_ahora),
            fecha_hora_devolucion = _ahora,
            estado_devolucion = if(_dias > 0, 'atrasado', 'devuelto'),
            dias_atraso = _dias,
            cobro_adicional = _dias * _precio
        where id_alquiler = _id_alquiler
          and fecha_devolucion_real is null;

        if row_count() > 0 then
            set _estado = if(_dias > 0, 'atrasado', 'devuelto');
            set _dias_atraso = _dias;
            set _cobro_adicional = _dias * _precio;
            set _fecha_hora_devolucion = _ahora;
        end if;
    end if;
end $$

create procedure sp_marcarvehiculodisponible(
    in _id_vehiculo int
)
begin
    update vehiculos
    set estado = 'disponible'
    where id_vehiculo = _id_vehiculo
      and estado = 'en_alquiler';
end $$

delimiter ;