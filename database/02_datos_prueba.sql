-- =====================================================================
-- PF-01 - Datos de prueba (12 libros y 8 préstamos)
-- Ejecutar después de 01_crear_estructura.sql
-- Las fechas son relativas a la fecha actual para que siempre haya
-- préstamos activos, vencidos y devueltos.
-- =====================================================================

INSERT INTO libros (titulo, autor, isbn, activo) VALUES
 ('Cien años de soledad',                 'Gabriel García Márquez',        '978-0307474728', TRUE),
 ('El Principito',                        'Antoine de Saint-Exupéry',      '978-0156012195', TRUE),
 ('Don Quijote de la Mancha',             'Miguel de Cervantes',           '978-8420412146', TRUE),
 ('Hombres de maíz',                      'Miguel Ángel Asturias',         '978-8437604947', TRUE),
 ('El señor Presidente',                  'Miguel Ángel Asturias',         '978-8437601953', TRUE),
 ('La casa de los espíritus',             'Isabel Allende',                '978-0553383805', TRUE),
 ('Rayuela',                              'Julio Cortázar',                '978-8420471891', TRUE),
 ('Pedro Páramo',                         'Juan Rulfo',                    '978-8437604091', TRUE),
 ('Crónica de una muerte anunciada',      'Gabriel García Márquez',        '978-0307387349', TRUE),
 ('Ficciones',                            'Jorge Luis Borges',             '978-8420633121', TRUE),
 ('El túnel',                             'Ernesto Sabato',                '978-8432216770', TRUE),
 ('Libro antiguo (fuera de circulación)', 'Autor Desconocido',             '978-0000000001', FALSE);

-- Préstamos devueltos (historial)
INSERT INTO prestamos (libro_id, nombre_lector, fecha_prestamo, fecha_devolucion_prevista, fecha_devolucion_real, estado) VALUES
 (1,  'María Fernanda López', CURRENT_DATE - 30, CURRENT_DATE - 23, CURRENT_DATE - 24, 'DEVUELTO'),
 (3,  'Carlos Méndez',        CURRENT_DATE - 25, CURRENT_DATE - 11, CURRENT_DATE - 12, 'DEVUELTO'),
 (6,  'Ana Lucía Pérez',      CURRENT_DATE - 20, CURRENT_DATE - 13, CURRENT_DATE - 10, 'DEVUELTO');

-- Préstamos activos dentro del plazo
INSERT INTO prestamos (libro_id, nombre_lector, fecha_prestamo, fecha_devolucion_prevista, fecha_devolucion_real, estado) VALUES
 (2,  'Luis Alberto Gómez',   CURRENT_DATE - 2,  CURRENT_DATE + 5,  NULL, 'ACTIVO'),
 (4,  'María Fernanda López', CURRENT_DATE - 1,  CURRENT_DATE + 13, NULL, 'ACTIVO');

-- Préstamos activos vencidos
INSERT INTO prestamos (libro_id, nombre_lector, fecha_prestamo, fecha_devolucion_prevista, fecha_devolucion_real, estado) VALUES
 (7,  'Pedro Ajú',            CURRENT_DATE - 18, CURRENT_DATE - 4,  NULL, 'ACTIVO'),
 (8,  'Sofía Ramírez',        CURRENT_DATE - 12, CURRENT_DATE - 5,  NULL, 'ACTIVO'),
 (10, 'Carlos Méndez',        CURRENT_DATE - 9,  CURRENT_DATE - 2,  NULL, 'ACTIVO');
