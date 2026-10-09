use concesionariadb_in4cm;

drop procedure if exists sp_buscarvehiculosdisponibles;
drop procedure if exists sp_marcarvehiculovendido;
drop procedure if exists sp_listarventasporasesor;

delimiter $$

-- busca por marca, modelo o placa, solo vehículos 'disponible' _operacion: 'venta' o 'alquiler' (NULL o '' = sin filtrar). 'ambas' siempre cumple. devuelve las mismas 16 columnas que sp_listarvehiculos para reutilizar mapearVehiculo()
create procedure sp_buscarvehiculosdisponibles(
    in _criterio varchar(50),
    in _operacion varchar(10)
)
begin
    select id_vehiculo, placa, marca, modelo, anio, color, condicion, proveedor, costo, observaciones, estado, progreso_taller, id_usuario_provisionador, fecha_ingreso, operacion_permitida, tipo
    from vehiculos
    where estado = 'disponible'
      and (_operacion is null or _operacion = '' or operacion_permitida in (_operacion, 'ambas'))
      and (_criterio is null or _criterio = ''
           or marca like concat('%', _criterio, '%')
           or modelo like concat('%', _criterio, '%')
           or placa like concat('%', _criterio, '%'))
    order by marca, modelo;
end $$

-- la venta NO borra el vehículo, solo cambia su estado a 'vendido' solo afecta una fila si está disponible y permite venta (si no, el DAO devuelve false)
create procedure sp_marcarvehiculovendido(
    in _id_vehiculo int
)
begin
    update vehiculos
    set estado = 'vendido'
    where id_vehiculo = _id_vehiculo
      and estado = 'disponible'
      and operacion_permitida in ('venta', 'ambas');
end $$

-- ventas de un asesor (historial) con datos del vehículo y del cliente
create procedure sp_listarventasporasesor(
    in _id_asesor int
)
begin
    select ve.id_venta, ve.id_vehiculo, ve.cui_cliente, ve.id_asesor, ve.precio, ve.fecha_venta,
           veh.placa,
           concat(veh.marca, ' ', veh.modelo, ' ', veh.anio),
           concat(c.nombres, ' ', c.apellidos)
    from ventas ve
    inner join vehiculos veh on ve.id_vehiculo = veh.id_vehiculo
    inner join clientes c on ve.cui_cliente = c.cui
    where ve.id_asesor = _id_asesor
    order by ve.fecha_venta desc, ve.id_venta desc;
end $$

delimiter ;