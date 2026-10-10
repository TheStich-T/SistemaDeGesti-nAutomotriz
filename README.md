# Impulso Automotriz — Sistema de Gestión de Concesionario (SGA)

Aplicación de escritorio construida con JavaFX y MySQL para la gestión del concesionario "Impulso Automotriz": usuarios, ingreso de vehículos, taller, ventas, alquileres, devoluciones, facturación y reportes. El concesionario maneja tanto **venta** como **alquiler** de vehículos.

## Propósito educativo

Este proyecto fue construido a nivel educativo para demostrar los alcances y aptitudes adquiridas por estudiantes de informática en el curso de Taller. Sirve como evidencia de aprendizaje sobre programación orientada a objetos, patrones de diseño, persistencia de datos, construcción de interfaces gráficas de escritorio y trabajo colaborativo con metodología Scrum.

A diferencia del proyecto anterior (Página Viva), aquí el equipo creó desde cero toda la documentación (visión, product backlog, DoR, DoD, diagrama de clases y esquema de base de datos) además del código.

## Contenido

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos](#requisitos)
- [Configuración de la base de datos](#configuración-de-la-base-de-datos)
- [Ejecución](#ejecución)
- [Roles y permisos](#roles-y-permisos)
- [Estados del vehículo](#estados-del-vehículo)
- [Metodología y flujo de ramas](#metodología-y-flujo-de-ramas)
- [Créditos](#créditos)

## Características

- **Autenticación:** inicio de sesión con contraseña cifrada en SHA-256.
- **Gestión de sesión:** el usuario actual está disponible durante toda la ejecución mediante `SessionContext`; cierre de sesión desde cada panel.
- **Dashboards por rol:** cada rol (administrador, provisionador, mecánico y asesor) tiene su propio panel con menú de navegación. El panel de administración muestra indicadores generales (ventas de hoy, alquileres activos y atrasados, vehículos disponibles, en taller y vendidos, usuarios activos).
- **Gestión de usuarios:** registro con usuario, contraseña y rol; edición, activación y desactivación de usuarios.
- **Ingreso de vehículos (provisionador):** formulario con marca, modelo, año, placa, color, condición (nuevo/usado), proveedor, costo y observaciones; al guardar se elige el destino inicial (En taller o Disponible) y la operación permitida (venta, alquiler o ambas). La placa no se puede repetir. El provisionador puede corregir o anular sus ingresos mientras el vehículo siga En taller o Disponible.
- **Taller (mecánico):** cola de vehículos En taller con marcado de avance (pendiente, en progreso, terminado); al terminar el trabajo el vehículo pasa a Disponible.
- **Búsqueda de vehículos disponibles:** el asesor consulta solo los vehículos en estado Disponible, según la operación (venta o alquiler).
- **Módulo de ventas:** formulario del cliente (CUI, nombres, apellidos, teléfono y licencia) y registro de la venta en un solo paso; factura mostrada en ventana (sin imprimir) y consulta posterior desde el historial de ventas.
- **Módulo de alquileres:** registro del alquiler con fecha de salida, fecha de regreso y seguro; ticket de alquiler en ventana y pantalla "Mis alquileres" para volver a verlo.
- **Devoluciones:** registro de la devolución con fecha y hora, estado (devuelto o atrasado), días de atraso y cobro adicional (días de atraso × tarifa por día). El vehículo puede volver a Disponible o enviarse a la cola del taller.
- **Tarifas de alquiler:** precio por día según el tipo de vehículo (sedán, hatchback, SUV, pickup y otro).
- **Reportes:** ventas y alquileres por período (día, semana o mes).
- **Validaciones:** mensajes de error y alertas uniformes mediante `ValidarException`.
- **Persistencia:** base de datos MySQL accedida mediante procedimientos almacenados (SP) y funciones.

## Tecnologías

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Interfaz gráfica | JavaFX 21 con vistas FXML y hojas de estilo CSS (diseño con Scene Builder) |
| Base de datos | MySQL 8 (procedimientos almacenados y funciones) |
| Conector | MySQL Connector/J 8 (`com.mysql.cj.jdbc.Driver`) |
| IDE | Apache NetBeans (proyecto Ant) |
| Gestión del proyecto | Scrum, Trello y Git/GitHub |

## Arquitectura

El proyecto sigue una arquitectura por capas que separa responsabilidades:

```
┌──────────────────────────────────────────────┐
│  Vista (FXML + CSS + Controladores)          │
│  src/org/sga/view · src/org/sga/controller   │
├──────────────────────────────────────────────┤
│  Lógica / Gestión de sesión y permisos       │
│  src/org/sga/system · src/org/sga/manager    │
├──────────────────────────────────────────────┤
│  Acceso a datos (DAO)                        │
│  src/org/sga/dao · src/org/sga/dao/impl      │
├──────────────────────────────────────────────┤
│  Modelo de entidades                         │
│  src/org/sga/model                           │
├──────────────────────────────────────────────┤
│  Base de datos MySQL (tablas, SPs y funciones)│
└──────────────────────────────────────────────┘
```

- **Modelo:** clases de entidad (`Usuario`, `Vehiculo`, `Cliente`, `Venta`, `Alquiler`) y clases de apoyo para reportes (`FilaReporte`, `Indicadores`, `PeriodoReporte`).
- **DAO:** interfaces (`CRUD`, `UsuarioDAO`, `VehiculoDAO`, `ClienteDAO`, `VentaDAO`, `AlquilerDAO`, `ReporteDAO`) e implementaciones en `dao/impl` que invocan los procedimientos almacenados.
- **Controladores:** lógica de las vistas JavaFX (eventos, tablas, validaciones y navegación).
- **Singleton:** `Conexion` (conexión a la BD) y `SessionContext` (usuario autenticado).
- **Permisos:** `RolPermisos` define a qué dashboard redirige el login según el rol.

## Estructura del proyecto

```
SistemaDeGesti-nAutomotriz/
├── Scripts SQL/
│   ├── concesionariadb_script_ddl.sql          # Base de datos, tablas y SPs de CRUD
│   ├── concesionariadb_script_dml.sql          # Datos de ejemplo (usuarios, clientes, tarifas, etc.)
│   ├── concesionariadb_script_login.sql        # SPs de inicio de sesión
│   ├── concesionariadb_script_ventas.sql       # SPs de ventas y búsqueda de disponibles
│   ├── concesionariadb_script_alquiler.sql     # SPs de alquiler
│   ├── concesionariadb_script_comprobantes.sql # SPs de clientes y comprobantes
│   ├── concesionariodb_scrip_devolucion.sql    # Columnas, función y SPs de devolución
│   └── concesionariadb_script_reportes.sql     # SPs de indicadores y reportes
├── src/
│   ├── db.properties                  # Credenciales locales (NO versionado)
│   └── org/sga/
│       ├── controller/                # Controladores JavaFX de cada vista
│       ├── dao/                       # Interfaces de acceso a datos
│       │   └── impl/                  # Implementaciones con MySQL
│       ├── exception/                 # ValidarException (validaciones centralizadas)
│       ├── manager/                   # AutenticacionService, SessionContext y RolPermisos
│       ├── model/                     # Entidades del dominio
│       ├── system/                    # Main (punto de entrada y navegación)
│       ├── util/                      # Conexion, SecurityUtil (hash SHA-256)
│       └── view/                      # Vistas FXML
│           └── style/                 # Hojas de estilo CSS
├── nbproject/                         # Configuración de NetBeans
├── db.properties.example              # Plantilla de credenciales (versionada)
├── build.xml                          # Script Ant
└── manifest.mf
```

## Requisitos

- **JDK 21** (o superior compatible con JavaFX 21).
- **JavaFX SDK 21** configurado en NetBeans como librería `JavaFX21`.
- **MySQL 8** y **MySQL Workbench** (recomendado para ejecutar los scripts).
- **Connector/J 8** (`mysql-connector-j`) agregado al proyecto como librería `MySQL_Connector_8`.
- **Apache NetBeans** como IDE.

## Configuración de la base de datos

### 1. Crear la base de datos

Ejecuta los scripts de la carpeta `Scripts SQL/` en MySQL Workbench **en este orden**:

1. `concesionariadb_script_ddl.sql`
2. `concesionariadb_script_dml.sql`
3. `concesionariadb_script_login.sql`
4. `concesionariadb_script_ventas.sql`
5. `concesionariadb_script_alquiler.sql`
6. `concesionariadb_script_comprobantes.sql`
7. `concesionariodb_scrip_devolucion.sql`
8. `concesionariadb_script_reportes.sql`

Esto crea la base de datos `concesionariadb_in4cm` con sus tablas, procedimientos almacenados, funciones, datos de ejemplo y usuarios de prueba.

### 2. Configurar la conexión

La conexión se lee desde `src/db.properties` (recurso copiado al classpath en la compilación):

```properties
db.url=jdbc:mysql://localhost:3306/concesionariadb_in4cm?serverTimezone=America/Guatemala
db.user=TU_USUARIO
db.password=TU_CONTRASEÑA
```

Para configurar tu entorno:

1. Copia `db.properties.example` como `src/db.properties`.
2. Ajusta los valores (URL, usuario y contraseña de tu MySQL).
3. Recompila para que el archivo se copie a `build/classes`.

> `src/db.properties` está en `.gitignore` y **no se versiona**: no subas credenciales reales al repositorio. Si el archivo falta o está incompleto, `Conexion` lanza un error claro al arrancar.

### Usuarios de prueba

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `draguay` | `admin123` | admin |
| `lsalazar` | `provisionador123` | provisionador |
| `aperez` | `mecanico123` | mecanico |
| `jsian` | `asesor123` | asesor |
| `mgarcia` | `asesor456` | asesor |

> Estos usuarios son solo para desarrollo y pruebas.

## Ejecución

Desde Apache NetBeans:

1. Abrir el proyecto (`File > Open Project`).
2. Verificar que las librerías `JavaFX21` y `MySQL_Connector_8` estén en el classpath.
3. Verificar que las opciones de ejecución apunten a tu JavaFX SDK (`Project Properties > Run > VM Options`):

```
   --module-path "C:\javafx-sdk-21.0.11\lib" --add-modules javafx.controls,javafx.fxml
```

   Ajusta la ruta a donde tengas instalado el SDK.
4. Configurar la base de datos y las credenciales.
5. Ejecutar la clase principal `org.sga.system.Main`.

## Roles y permisos

El sistema distingue cuatro roles:

| Rol | Acceso |
|-----|--------|
| `admin` | Todo lo de los demás roles, además de gestión de usuarios (crear usuarios y asignar roles), indicadores y reportes |
| `provisionador` | Ingreso de vehículos, con corrección y anulación de sus propios ingresos |
| `mecanico` | Cola del taller, marcado de avance y cierre del trabajo |
| `asesor` | Búsqueda de vehículos disponibles, ventas, alquileres, devoluciones, historial de ventas y "Mis alquileres" |

El login redirige a cada rol a su propio dashboard.

## Estados del vehículo

| Estado | Descripción |
|--------|-------------|
| `en_taller` | El vehículo está en revisión o reparación |
| `disponible` | Listo para venderse o alquilarse |
| `en_alquiler` | Alquilado a un cliente |
| `vendido` | Venta realizada |

Transiciones principales: un vehículo En taller pasa a Disponible cuando el mecánico termina su trabajo; Disponible pasa a Vendido o En alquiler; y al regresar un alquiler vuelve a Disponible o a En taller si se envía a revisión.

## Metodología y flujo de ramas

El proyecto se desarrolló con Scrum en un Sprint 0 de preparación y 3 sprints, usando un tablero de Trello para el product backlog y el seguimiento de tareas:

**Tablero de Trello:** [Proyecto Concesionaria Impulso Automotriz](https://trello.com/invite/b/6aaf44f9c21864ed87e6883d/ATTI7ed7463dc673fda038d5bd5a8d84bb35D25C3A9A/proyecto-concesionaria-impulso-automotriz)

- `test`: rama final donde se integra y se deja el proyecto completo para su entrega.
- `develop`: rama de integración de las funcionalidades durante los sprints.
- `ft/<funcionalidad>`: una rama por cada funcionalidad (por ejemplo `ft/login` o `ft/vehiculo-alta`), que se integra a `develop` al terminar.
- Flujo de tarjetas: Product backlog → Sprint backlog → Asignado → Trabajando → Prueba → Incremento.

## Créditos

Proyecto educativo desarrollado para el curso de Taller (estudiantes de informática). Los alumnos demuestran aquí competencias en: programación orientada a objetos, persistencia de datos con JDBC y procedimientos almacenados, construcción de interfaces con JavaFX y trabajo colaborativo con Git y metodología de Scrum.

| Nombre | Rol | Usuario de GitHub |
|--------|-----|-------------------|
| Diego Raguay | Scrum Master y desarrollador | [RaguayDiego-02](https://github.com/RaguayDiego-02) |
| Levi Salazar | Desarrollador | [levi-Salazar432](https://github.com/levi-Salazar432) |
| Antony Pérez | Desarrollador | [TheStich-T](https://github.com/TheStich-T) |
| Javier Sian | Desarrollador | [srrJavier](https://github.com/srrJavier) |
