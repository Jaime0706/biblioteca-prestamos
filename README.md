# Biblioteca Comunitaria – Préstamos de libros

**Estudiante:** Jaime Alejandro Monzón Contreras
**Carné:** 9941-24-11049
**Proyecto asignado:** [PF-01] – Préstamos de una biblioteca comunitaria

## Descripción
Sistema para controlar los libros de una biblioteca y los préstamos a sus lectores.

## Tecnologías
Java, Spring Boot, Maven, Spring Data JPA, PostgreSQL.

## Estado: Avance 1 (11 de octubre de 2026)
- Conexión con PostgreSQL y creación de tablas con JPA.
- Entidades `Libro` y `Prestamo` con su relación.
- API REST: registrar y consultar libros (`POST` y `GET /api/libros`), probado en Postman.

## Configuración local
Crear la base de datos `biblioteca_db` en PostgreSQL y un archivo
`src/main/resources/application-local.properties` con la URL, usuario y
contraseña (este archivo no se sube al repositorio).
