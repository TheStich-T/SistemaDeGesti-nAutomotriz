-- drop database if exists concesionariadb_in4cm;
create database if not exists concesionariadb_in4cm;
use concesionariadb_in4cm;

-- =============================================================================
-- creación de tablas
-- =============================================================================

create table usuarios (
	id_usuario int primary key auto_increment,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    rol enum('admin', 'provisionador', 'mecanico', 'asesor') not null,
    activo boolean default true,
    fecha_creacion timestamp default current_timestamp
);

create table clientes (
	cui bigint primary key,
    nombres varchar(100) not null,
    apellidos varchar(100) not null,
    telefono varchar(15),
    correo varchar(100),
    licencia varchar(30) not null
);

create table vehiculos (
	id_vehiculo int primary key auto_increment,
    placa varchar(15) not null unique,
    marca varchar(50) not null,
    modelo varchar(50) not null,
    anio int not null,
    color varchar(30),
    condicion enum('nuevo', 'usado') not null,
    proveedor varchar(100),
    costo decimal(10,2) not null,
    observaciones text,
    estado enum('en_taller', 'disponible', 'en_alquiler', 'vendido') not null default 'en_taller',
    progreso_taller enum('pendiente', 'en_progreso', 'terminado') default 'pendiente',
    id_usuario_provisionador int,
    fecha_ingreso timestamp default current_timestamp
);

create table reportes_taller (
	id_reporte int primary key auto_increment,
    id_vehiculo int,
    id_mecanico int,
    diagnostico text,
    trabajo_realizado text,
    repuestos text,
    notas text,
    fecha_reporte timestamp default current_timestamp
);

create table ventas (
	id_venta int primary key auto_increment,
    id_vehiculo int,
    cui_cliente bigint,
    id_asesor int,
    precio decimal(10,2) not null,
    fecha_venta timestamp default current_timestamp
);

create table alquileres (
	id_alquiler int primary key auto_increment,
    id_vehiculo int,
    cui_cliente bigint,
    id_asesor int,
    fecha_salida date not null,
    fecha_regreso date not null,
    lleva_seguro boolean default false,
    fecha_devolucion_real date null,
    fecha_registro timestamp default current_timestamp
);

-- =============================================================================
-- llaves foráneas
-- =============================================================================

alter table vehiculos
add constraint fk_v_provisionador foreign key (id_usuario_provisionador) references usuarios(id_usuario) on delete restrict;

alter table reportes_taller
add constraint fk_r_vehiculo foreign key (id_vehiculo) references vehiculos(id_vehiculo) on delete cascade,
add constraint fk_r_mecanico foreign key (id_mecanico) references usuarios(id_usuario) on delete restrict;

alter table ventas
add constraint fk_ve_vehiculo foreign key (id_vehiculo) references vehiculos(id_vehiculo) on delete restrict,
add constraint fk_ve_cliente foreign key (cui_cliente) references clientes(cui) on delete restrict,
add constraint fk_ve_asesor foreign key (id_asesor) references usuarios(id_usuario) on delete restrict;

alter table alquileres
add constraint fk_al_vehiculo foreign key (id_vehiculo) references vehiculos(id_vehiculo) on delete restrict,
add constraint fk_al_cliente foreign key (cui_cliente) references clientes(cui) on delete restrict,
add constraint fk_al_asesor foreign key (id_asesor) references usuarios(id_usuario) on delete restrict;

use concesionariadb_in4cm;

-- =============================================================================
-- 1. crud: usuarios
-- =============================================================================
delimiter $$

create procedure sp_insertarusuario(
    in _username varchar(50),
    in _password_hash varchar(255),
    in _rol varchar(20)
)
begin
    insert into usuarios(username, password_hash, rol)
    values (_username, _password_hash, _rol);
end $$

create procedure sp_listarusuarios()
begin
    select id_usuario, username, rol, activo, fecha_creacion from usuarios;
end $$

create procedure sp_buscarusuario(
    in _id_usuario int
)
begin
    select id_usuario, username, rol, activo, fecha_creacion
    from usuarios
    where id_usuario = _id_usuario;
end $$

create procedure sp_actualizarusuario(
    in _id_usuario int,
    in _username varchar(50),
    in _rol varchar(20),
    in _activo boolean
)
begin
    update usuarios
    set username = _username,
        rol = _rol,
        activo = _activo
    where id_usuario = _id_usuario;
end $$

create procedure sp_eliminarusuario(
    in _id_usuario int
)
begin
    delete from usuarios where id_usuario = _id_usuario;
end $$

delimiter ;

-- =============================================================================
-- 2. crud: clientes
-- =============================================================================
delimiter $$

create procedure sp_insertarcliente(
    in _cui bigint,
    in _nombres varchar(100),
    in _apellidos varchar(100),
    in _telefono varchar(15),
    in _correo varchar(100),
    in _licencia varchar(30)
)
begin
    insert into clientes(cui, nombres, apellidos, telefono, correo, licencia)
    values (_cui, _nombres, _apellidos, _telefono, _correo, _licencia);
end $$

create procedure sp_listarclientes()
begin
    select cui, nombres, apellidos, telefono, correo, licencia from clientes;
end $$

create procedure sp_buscarcliente(
    in _cui bigint
)
begin
    select cui, nombres, apellidos, telefono, correo, licencia
    from clientes
    where cui = _cui;
end $$

create procedure sp_actualizarcliente(
    in _cui bigint,
    in _nombres varchar(100),
    in _apellidos varchar(100),
    in _telefono varchar(15),
    in _correo varchar(100),
    in _licencia varchar(30)
)
begin
    update clientes
    set nombres = _nombres,
        apellidos = _apellidos,
        telefono = _telefono,
        correo = _correo,
        licencia = _licencia
    where cui = _cui;
end $$

create procedure sp_eliminarcliente(
    in _cui bigint
)
begin
    delete from clientes where cui = _cui;
end $$

delimiter ;

-- =============================================================================
-- 3. crud: vehiculos
-- =============================================================================
delimiter $$

create procedure sp_insertarvehiculo(
    in _placa varchar(15),
    in _marca varchar(50),
    in _modelo varchar(50),
    in _anio int,
    in _color varchar(30),
    in _condicion varchar(10),
    in _proveedor varchar(100),
    in _costo decimal(10,2),
    in _observaciones text,
    in _estado varchar(20),
    in _id_usuario_provisionador int
)
begin
    insert into vehiculos(placa, marca, modelo, anio, color, condicion, proveedor, costo, observaciones, estado, id_usuario_provisionador)
    values (_placa, _marca, _modelo, _anio, _color, _condicion, _proveedor, _costo, _observaciones, _estado, _id_usuario_provisionador);
end $$

create procedure sp_listarvehiculos()
begin
    select id_vehiculo, placa, marca, modelo, anio, color, condicion, proveedor, costo, observaciones, estado, progreso_taller, id_usuario_provisionador, fecha_ingreso
    from vehiculos;
end $$

create procedure sp_buscarvehiculo(
    in _id_vehiculo int
)
begin
    select id_vehiculo, placa, marca, modelo, anio, color, condicion, proveedor, costo, observaciones, estado, progreso_taller, id_usuario_provisionador, fecha_ingreso
    from vehiculos
    where id_vehiculo = _id_vehiculo;
end $$

create procedure sp_actualizarvehiculo(
    in _id_vehiculo int,
    in _placa varchar(15),
    in _marca varchar(50),
    in _modelo varchar(50),
    in _anio int,
    in _color varchar(30),
    in _condicion varchar(10),
    in _proveedor varchar(100),
    in _costo decimal(10,2),
    in _observaciones text
)
begin
    update vehiculos
    set placa = _placa,
        marca = _marca,
        modelo = _modelo,
        anio = _anio,
        color = _color,
        condicion = _condicion,
        proveedor = _proveedor,
        costo = _costo,
        observaciones = _observaciones
    where id_vehiculo = _id_vehiculo
      and estado in ('en_taller', 'disponible');
end $$

-- anular ingreso: solo si el auto sigue en_taller o disponible
create procedure sp_eliminarvehiculo(
    in _id_vehiculo int
)
begin
    delete from vehiculos
    where id_vehiculo = _id_vehiculo
      and estado in ('en_taller', 'disponible');
end $$

delimiter ;

-- =============================================================================
-- 4. crud: reportes_taller
-- =============================================================================
delimiter $$

create procedure sp_insertarreportetaller(
    in _id_vehiculo int,
    in _id_mecanico int,
    in _diagnostico text,
    in _trabajo_realizado text,
    in _repuestos text,
    in _notas text
)
begin
    insert into reportes_taller(id_vehiculo, id_mecanico, diagnostico, trabajo_realizado, repuestos, notas)
    values (_id_vehiculo, _id_mecanico, _diagnostico, _trabajo_realizado, _repuestos, _notas);
end $$

create procedure sp_listarreportestaller()
begin
    select id_reporte, id_vehiculo, id_mecanico, diagnostico, trabajo_realizado, repuestos, notas, fecha_reporte
    from reportes_taller;
end $$

create procedure sp_buscarreportetaller(
    in _id_reporte int
)
begin
    select id_reporte, id_vehiculo, id_mecanico, diagnostico, trabajo_realizado, repuestos, notas, fecha_reporte
    from reportes_taller
    where id_reporte = _id_reporte;
end $$

create procedure sp_actualizarreportetaller(
    in _id_reporte int,
    in _diagnostico text,
    in _trabajo_realizado text,
    in _repuestos text,
    in _notas text
)
begin
    update reportes_taller
    set diagnostico = _diagnostico,
        trabajo_realizado = _trabajo_realizado,
        repuestos = _repuestos,
        notas = _notas
    where id_reporte = _id_reporte;
end $$

create procedure sp_eliminarreportetaller(
    in _id_reporte int
)
begin
    delete from reportes_taller where id_reporte = _id_reporte;
end $$

delimiter ;

-- =============================================================================
-- 5. crud: ventas
-- =============================================================================
delimiter $$

create procedure sp_insertarventa(
    in _id_vehiculo int,
    in _cui_cliente bigint,
    in _id_asesor int,
    in _precio decimal(10,2)
)
begin
    insert into ventas(id_vehiculo, cui_cliente, id_asesor, precio)
    values (_id_vehiculo, _cui_cliente, _id_asesor, _precio);
end $$

create procedure sp_listarventas()
begin
    select id_venta, id_vehiculo, cui_cliente, id_asesor, precio, fecha_venta from ventas;
end $$

create procedure sp_buscarventa(
    in _id_venta int
)
begin
    select id_venta, id_vehiculo, cui_cliente, id_asesor, precio, fecha_venta
    from ventas
    where id_venta = _id_venta;
end $$

create procedure sp_actualizarventa(
    in _id_venta int,
    in _precio decimal(10,2)
)
begin
    update ventas set precio = _precio where id_venta = _id_venta;
end $$

create procedure sp_eliminarventa(
    in _id_venta int
)
begin
    delete from ventas where id_venta = _id_venta;
end $$

delimiter ;

-- =============================================================================
-- 6. crud: alquileres
-- =============================================================================
delimiter $$

create procedure sp_insertaralquiler(
    in _id_vehiculo int,
    in _cui_cliente bigint,
    in _id_asesor int,
    in _fecha_salida date,
    in _fecha_regreso date,
    in _lleva_seguro boolean
)
begin
    insert into alquileres(id_vehiculo, cui_cliente, id_asesor, fecha_salida, fecha_regreso, lleva_seguro)
    values (_id_vehiculo, _cui_cliente, _id_asesor, _fecha_salida, _fecha_regreso, _lleva_seguro);
end $$

create procedure sp_listaralquileres()
begin
    select id_alquiler, id_vehiculo, cui_cliente, id_asesor, fecha_salida, fecha_regreso, lleva_seguro, fecha_devolucion_real, fecha_registro
    from alquileres;
end $$

create procedure sp_buscaralquiler(
    in _id_alquiler int
)
begin
    select id_alquiler, id_vehiculo, cui_cliente, id_asesor, fecha_salida, fecha_regreso, lleva_seguro, fecha_devolucion_real, fecha_registro
    from alquileres
    where id_alquiler = _id_alquiler;
end $$

create procedure sp_actualizaralquiler(
    in _id_alquiler int,
    in _fecha_salida date,
    in _fecha_regreso date,
    in _lleva_seguro boolean
)
begin
    update alquileres
    set fecha_salida = _fecha_salida,
        fecha_regreso = _fecha_regreso,
        lleva_seguro = _lleva_seguro
    where id_alquiler = _id_alquiler;
end $$

create procedure sp_eliminaralquiler(
    in _id_alquiler int
)
begin
    delete from alquileres where id_alquiler = _id_alquiler;
end $$

delimiter ;

-- =============================================================================
-- 7. procedimientos de negocio (cambian el estado del vehículo)
-- =============================================================================
delimiter $$

-- asesor: vender un vehículo disponible
create procedure sp_vendervehiculo(
    in _id_vehiculo int,
    in _cui_cliente bigint,
    in _id_asesor int,
    in _precio decimal(10,2)
)
begin
    insert into ventas(id_vehiculo, cui_cliente, id_asesor, precio)
    select id_vehiculo, _cui_cliente, _id_asesor, _precio
    from vehiculos
    where id_vehiculo = _id_vehiculo and estado = 'disponible';

    update vehiculos set estado = 'vendido' where id_vehiculo = _id_vehiculo and estado = 'disponible';
end $$

-- asesor: alquilar un vehículo disponible
create procedure sp_alquilarvehiculo(
    in _id_vehiculo int,
    in _cui_cliente bigint,
    in _id_asesor int,
    in _fecha_salida date,
    in _fecha_regreso date,
    in _lleva_seguro boolean
)
begin
    insert into alquileres(id_vehiculo, cui_cliente, id_asesor, fecha_salida, fecha_regreso, lleva_seguro)
    select id_vehiculo, _cui_cliente, _id_asesor, _fecha_salida, _fecha_regreso, _lleva_seguro
    from vehiculos
    where id_vehiculo = _id_vehiculo and estado = 'disponible';

    update vehiculos set estado = 'en_alquiler' where id_vehiculo = _id_vehiculo and estado = 'disponible';
end $$

-- asesor: registrar el regreso de un vehículo alquilado (devuelto o enviado a revisión)
create procedure sp_devolveralquiler(
    in _id_alquiler int,
    in _id_vehiculo int,
    in _enviar_a_taller boolean
)
begin
    update alquileres
    set fecha_devolucion_real = curdate()
    where id_alquiler = _id_alquiler;

    if _enviar_a_taller then
        update vehiculos set estado = 'en_taller', progreso_taller = 'pendiente' where id_vehiculo = _id_vehiculo;
    else
        update vehiculos set estado = 'disponible' where id_vehiculo = _id_vehiculo;
    end if;
end $$

-- mecánico: actualizar avance en la pantalla "Cola"
create procedure sp_actualizarprogresotaller(
    in _id_vehiculo int,
    in _progreso_taller varchar(20)
)
begin
    update vehiculos set progreso_taller = _progreso_taller where id_vehiculo = _id_vehiculo;
end $$

-- mecánico: cerrar el reporte y liberar el vehículo a Disponible
create procedure sp_cerrarreportetaller(
    in _id_vehiculo int,
    in _id_mecanico int,
    in _diagnostico text,
    in _trabajo_realizado text,
    in _repuestos text,
    in _notas text
)
begin
    insert into reportes_taller(id_vehiculo, id_mecanico, diagnostico, trabajo_realizado, repuestos, notas)
    values (_id_vehiculo, _id_mecanico, _diagnostico, _trabajo_realizado, _repuestos, _notas);

    update vehiculos set estado = 'disponible', progreso_taller = 'terminado' where id_vehiculo = _id_vehiculo;
end $$

delimiter ;

use concesionariadb_in4cm;

-- =============================================================================
-- vistas
-- =============================================================================

create or replace view vw_lista_usuarios as
select
    id_usuario as 'id usuario',
    username as 'usuario',
    rol as 'rol',
    activo as 'activo',
    fecha_creacion as 'fecha de creación'
from usuarios;

create or replace view vw_lista_clientes as
select
    cui as 'cui',
    concat(nombres, ' ', apellidos) as 'cliente',
    telefono as 'teléfono',
    correo as 'correo',
    licencia as 'licencia'
from clientes;

create or replace view vw_lista_vehiculos as
select
    v.id_vehiculo as 'id vehículo',
    v.placa as 'placa',
    v.marca as 'marca',
    v.modelo as 'modelo',
    v.anio as 'año',
    v.color as 'color',
    v.condicion as 'condición',
    v.estado as 'estado',
    v.progreso_taller as 'progreso taller',
    u.username as 'provisionador',
    v.fecha_ingreso as 'fecha de ingreso'
from vehiculos v
inner join usuarios u on v.id_usuario_provisionador = u.id_usuario;

create or replace view vw_lista_reportes_taller as
select
    r.id_reporte as 'id reporte',
    veh.placa as 'placa',
    u.username as 'mecánico',
    r.diagnostico as 'diagnóstico',
    r.trabajo_realizado as 'trabajo realizado',
    r.repuestos as 'repuestos',
    r.notas as 'notas',
    r.fecha_reporte as 'fecha'
from reportes_taller r
inner join vehiculos veh on r.id_vehiculo = veh.id_vehiculo
inner join usuarios u on r.id_mecanico = u.id_usuario;

create or replace view vw_lista_ventas as
select
    ve.id_venta as 'no. venta',
    veh.placa as 'placa',
    concat(c.nombres, ' ', c.apellidos) as 'cliente',
    u.username as 'asesor',
    ve.precio as 'precio',
    ve.fecha_venta as 'fecha'
from ventas ve
inner join vehiculos veh on ve.id_vehiculo = veh.id_vehiculo
inner join clientes c on ve.cui_cliente = c.cui
inner join usuarios u on ve.id_asesor = u.id_usuario;

create or replace view vw_lista_alquileres as
select
    al.id_alquiler as 'no. alquiler',
    veh.placa as 'placa',
    concat(c.nombres, ' ', c.apellidos) as 'cliente',
    u.username as 'asesor',
    al.fecha_salida as 'fecha de salida',
    al.fecha_regreso as 'fecha de regreso',
    al.lleva_seguro as 'seguro',
    al.fecha_devolucion_real as 'devuelto el'
from alquileres al
inner join vehiculos veh on al.id_vehiculo = veh.id_vehiculo
inner join clientes c on al.cui_cliente = c.cui
inner join usuarios u on al.id_asesor = u.id_usuario;

-- factura de venta (pantalla del asesor, no se imprime)
create or replace view vw_factura_venta as
select
    ve.id_venta as 'numero_factura',
    ve.fecha_venta as 'fecha_emision',
    c.cui as 'cui_cliente',
    concat(c.nombres, ' ', c.apellidos) as 'nombre_cliente',
    c.telefono as 'telefono_cliente',
    veh.placa as 'placa',
    concat(veh.marca, ' ', veh.modelo, ' ', veh.anio) as 'descripcion_vehiculo',
    ve.precio as 'total'
from ventas ve
inner join clientes c on ve.cui_cliente = c.cui
inner join vehiculos veh on ve.id_vehiculo = veh.id_vehiculo;

-- comprobante de alquiler (pantalla del asesor, no se imprime)
create or replace view vw_comprobante_alquiler as
select
    al.id_alquiler as 'numero_comprobante',
    al.fecha_registro as 'fecha_emision',
    c.cui as 'cui_cliente',
    concat(c.nombres, ' ', c.apellidos) as 'nombre_cliente',
    c.licencia as 'licencia_cliente',
    veh.placa as 'placa',
    concat(veh.marca, ' ', veh.modelo, ' ', veh.anio) as 'descripcion_vehiculo',
    al.fecha_salida as 'fecha_salida',
    al.fecha_regreso as 'fecha_regreso_prevista',
    al.lleva_seguro as 'incluye_seguro'
from alquileres al
inner join clientes c on al.cui_cliente = c.cui
inner join vehiculos veh on al.id_vehiculo = veh.id_vehiculo;