package com.biblioteca.prestamos.repository;

import com.biblioteca.prestamos.entity.EstadoPrestamo;
import com.biblioteca.prestamos.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    long countByActivoTrue();

    // Filtro: búsqueda por título o autor
    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrderByIdAsc(String titulo, String autor);

    // Filtro: libros activos que no tienen un préstamo en el estado indicado (normalmente ACTIVO)
    @Query("select l from Libro l where l.activo = true "
            + "and not exists (select p.id from Prestamo p where p.libro = l and p.estado = :estado) "
            + "order by l.id")
    List<Libro> findDisponibles(@Param("estado") EstadoPrestamo estado);
}
