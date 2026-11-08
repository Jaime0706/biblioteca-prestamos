-- =====================================================================
-- PF-01 Préstamos de una biblioteca comunitaria - Estructura de la BD
-- Motor: PostgreSQL
-- Uso:
--   1) Crear la base (ejecutar conectado a la base "postgres"):
--        CREATE DATABASE biblioteca_db;
--   2) Conectarse a biblioteca_db y ejecutar este script.
-- Nota: el backend también puede crear las tablas con JPA (ddl-auto=update);
--       este script es la referencia oficial de la estructura.
-- =====================================================================

DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS libros;

CREATE TABLE libros (
    id      BIGSERIAL    PRIMARY KEY,
    titulo  VARCHAR(150) NOT NULL,
    autor   VARCHAR(100) NOT NULL,
    isbn    VARCHAR(20)  NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_libros_isbn UNIQUE (isbn)
);

CREATE TABLE prestamos (
    id                         BIGSERIAL    PRIMARY KEY,
    libro_id                   BIGINT       NOT NULL,
    nombre_lector              VARCHAR(100) NOT NULL,
    fecha_prestamo             DATE         NOT NULL,
    fecha_devolucion_prevista  DATE         NOT NULL,
    fecha_devolucion_real      DATE,
    estado                     VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_prestamos_libro FOREIGN KEY (libro_id) REFERENCES libros (id),
    CONSTRAINT ck_prestamos_estado CHECK (estado IN ('ACTIVO', 'DEVUELTO')),
    CONSTRAINT ck_prestamos_fechas CHECK (fecha_devolucion_prevista >= fecha_prestamo),
    CONSTRAINT ck_prestamos_devolucion CHECK (
        (estado = 'DEVUELTO' AND fecha_devolucion_real IS NOT NULL)
        OR (estado = 'ACTIVO' AND fecha_devolucion_real IS NULL))
);

-- Regla de negocio a nivel de base de datos:
-- un libro no puede tener dos préstamos ACTIVOS al mismo tiempo.
CREATE UNIQUE INDEX uq_prestamo_activo_por_libro
    ON prestamos (libro_id)
    WHERE estado = 'ACTIVO';

CREATE INDEX idx_prestamos_estado ON prestamos (estado);
CREATE INDEX idx_prestamos_lector ON prestamos (LOWER(nombre_lector));
