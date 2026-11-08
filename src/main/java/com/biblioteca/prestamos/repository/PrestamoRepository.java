package com.biblioteca.prestamos.repository;

import com.biblioteca.prestamos.entity.EstadoPrestamo;
import com.biblioteca.prestamos.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    boolean existsByLibroIdAndEstado(Long libroId, EstadoPrestamo estado);

    long countByEstado(EstadoPrestamo estado);

    long countByEstadoAndFechaDevolucionPrevistaBefore(EstadoPrestamo estado, LocalDate fecha);

    // Filtro: préstamos por estado (activos)
    List<Prestamo> findByEstadoOrderByIdDesc(EstadoPrestamo estado);

    // Filtro: préstamos vencidos (activos cuya fecha prevista ya pasó)
    List<Prestamo> findByEstadoAndFechaDevolucionPrevistaBeforeOrderByFechaDevolucionPrevistaAsc(
            EstadoPrestamo estado, LocalDate fecha);

    // Filtro: préstamos por nombre del lector
    List<Prestamo> findByNombreLectorContainingIgnoreCaseOrderByIdDesc(String nombreLector);

    // Ids de los libros que tienen un préstamo en el estado indicado
    @Query("select p.libro.id from Prestamo p where p.estado = :estado")
    List<Long> findLibroIdsByEstado(@Param("estado") EstadoPrestamo estado);
}
