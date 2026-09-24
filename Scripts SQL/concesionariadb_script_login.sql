use concesionariadb_in4cm;

-- la tabla usuarios ya se creó en el DDL, porque vehiculos, reportes_taller,
-- ventas y alquileres dependen de ella por llave foránea

-- =============================================================================
-- procedimiento para iniciar sesión
-- =============================================================================
delimiter $$
create procedure sp_iniciar_sesion(
    in _username varchar(50),
    in _password_hash varchar(255)
)
drop procedure if exists sp_buscarusuarioporusername;
delimiter $$
create procedure sp_buscarusuarioporusername(
    in _username varchar(50)
)
begin
    select id_usuario, username, rol, password_hash, activo
    from usuarios
    where username = _username;
end $$
delimiter ;

begin
    select id_usuario, username, rol
    from usuarios
    where username = _username
      and password_hash = _password_hash
      and activo = true
    limit 1;
end $$
delimiter ;

-- =============================================================================
-- prueba de inicio de sesión con las cuentas creadas en el DML
-- =============================================================================
call sp_iniciar_sesion('draguay', sha2('admin123', 256));
call sp_iniciar_sesion('lsalazar', sha2('provisionador123', 256));
call sp_iniciar_sesion('aperez', sha2('mecanico123', 256));
call sp_iniciar_sesion('jsian', sha2('asesor123', 256));

select * from usuarios;