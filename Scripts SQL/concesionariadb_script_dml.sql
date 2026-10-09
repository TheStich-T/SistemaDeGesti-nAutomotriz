use concesionariadb_in4cm;

-- 1. usuarios (inserts directos)
INSERT INTO usuarios (username, password_hash, rol) VALUES
('draguay', sha2('admin123', 256), 'admin'),
('lsalazar', sha2('provisionador123', 256), 'provisionador'),
('aperez', sha2('mecanico123', 256), 'mecanico'),
('jsian', sha2('asesor123', 256), 'asesor'),
('mgarcia', sha2('asesor456', 256), 'asesor');

-- 2. clientes (inserts directos)
INSERT INTO clientes (cui, nombres, apellidos, telefono, correo, licencia) VALUES
(2500100010101, 'Ana', 'López', '55011001', 'ana.l@gmail.com', 'GT-LIC-0001'),
(2500100020101, 'Carlos', 'Méndez', '55011002', 'cmendez@yahoo.com', 'GT-LIC-0002'),
(2500100030101, 'Luis', 'Pérez', '55011003', 'lperez@hotmail.com', 'GT-LIC-0003'),
(2500100040101, 'María', 'García', '55011004', 'mgarcia@gmail.com', 'GT-LIC-0004'),
(2500100050101, 'Jorge', 'Castillo', '55011005', 'jcastillo@gmail.com', 'GT-LIC-0005'),
(2500100060101, 'Lucía', 'Fernández', '55011006', 'lfernandez@yahoo.com', 'GT-LIC-0006'),
(2500100070101, 'Mario', 'Gómez', '55011007', 'mgomez@gmail.com', 'GT-LIC-0007'),
(2500100080101, 'Elena', 'Morales', '55011008', 'emorales@hotmail.com', 'GT-LIC-0008');

-- 3. vehiculos (inserts directos)
INSERT INTO vehiculos (placa, marca, modelo, tipo, anio, color, condicion, proveedor, costo, observaciones, estado, progreso_taller, id_usuario_provisionador, operacion_permitida) VALUES
('P001ABC', 'Toyota', 'Corolla', 'sedan', 2024, 'Blanco', 'nuevo', 'Toyota Guatemala', 95000.00, NULL, 'disponible', 'terminado', 2, 'ambas'),
('P002ABC', 'Honda', 'Civic', 'sedan', 2023, 'Gris', 'usado', 'Auto Import SA', 78000.00, 'Un dueño anterior', 'disponible', 'terminado', 2, 'venta'),
('P003ABC', 'Mazda', '3', 'sedan', 2024, 'Rojo', 'nuevo', 'Mazda Centroamérica', 105000.00, NULL, 'en_taller', 'pendiente', 2, 'ambas'),
('P004ABC', 'Kia', 'Sportage', 'suv', 2022, 'Negro', 'usado', 'Auto Import SA', 130000.00, 'Golpe leve en puerta', 'en_taller', 'en_progreso', 2, 'alquiler'),
('P005ABC', 'Nissan', 'Sentra', 'sedan', 2023, 'Azul', 'usado', 'Nissan GT', 82000.00, NULL, 'en_alquiler', 'terminado', 2, 'alquiler'),
('P006ABC', 'Hyundai', 'Tucson', 'suv', 2024, 'Blanco', 'nuevo', 'Hyundai GT', 150000.00, NULL, 'vendido', 'terminado', 2, 'venta'),
('P007ABC', 'Chevrolet', 'Onix', 'hatchback', 2023, 'Plata', 'usado', 'Auto Import SA', 68000.00, NULL, 'disponible', 'terminado', 2, 'ambas'),
('P008ABC', 'Toyota', 'Hilux', 'pickup', 2024, 'Negro', 'nuevo', 'Toyota Guatemala', 210000.00, NULL, 'en_alquiler', 'terminado', 2, 'ambas');

-- 4. reportes_taller (inserts directos, solo autos que ya pasaron por taller)
INSERT INTO reportes_taller (id_vehiculo, id_mecanico, diagnostico, trabajo_realizado, repuestos, notas) VALUES
(1, 3, 'Revisión de ingreso, sin fallas', 'Chequeo general', 'Ninguno', 'Auto nuevo, listo para venta'),
(2, 3, 'Frenos desgastados', 'Cambio de pastillas de freno', 'Pastillas delanteras', 'Quedó en buen estado');

-- 5. ventas (inserts directos)
INSERT INTO ventas (id_vehiculo, cui_cliente, id_asesor, precio) VALUES
(6, 2500100010101, 4, 152000.00);

-- 5.1 tarifas_alquiler (precio por día según el tipo de vehículo, deben existir antes de insertar alquileres)
INSERT INTO tarifas_alquiler (tipo, precio_dia) VALUES
('sedan', 250.00),
('hatchback', 200.00),
('suv', 350.00),
('pickup', 400.00),
('otro', 300.00);

-- 6. alquileres (inserts directos)
INSERT INTO alquileres (id_vehiculo, cui_cliente, id_asesor, fecha_salida, fecha_regreso, lleva_seguro) VALUES
(5, 2500100020101, 4, '2026-09-15', '2026-09-22', true),
(8, 2500100030101, 5, '2026-09-18', '2026-09-25', false);

-- 7. registros adicionales usando los procedimientos (CALL)
CALL sp_insertarcliente(2500100090101, 'Pedro', 'Ramírez', '55011009', 'pramirez@gmail.com', 'GT-LIC-0009');
CALL sp_insertarcliente(2500100100101, 'Sofía', 'Vásquez', '55011010', 'svasquez@gmail.com', 'GT-LIC-0010');

CALL sp_insertarvehiculo('P009ABC', 'Ford', 'Escape', 2023, 'Gris', 'usado', 'Auto Import SA', 120000.00, NULL, 'disponible', 2, 'ambas', 'suv');
CALL sp_insertarvehiculo('P010ABC', 'Subaru', 'Forester', 2024, 'Verde', 'nuevo', 'Subaru GT', 175000.00, NULL, 'en_taller', 2, 'venta', 'suv');

-- venta usando el procedimiento de negocio (cambia el estado a vendido)
CALL sp_vendervehiculo(7, 2500100090101, 4, 70000.00);

-- alquiler usando el procedimiento de negocio (cambia el estado a en_alquiler)
CALL sp_alquilarvehiculo(9, 2500100100101, 5, '2026-09-20', '2026-09-27', true);

select * from vw_lista_vehiculos;