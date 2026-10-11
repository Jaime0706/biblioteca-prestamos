package com.biblioteca.prestamos.repository;

import com.biblioteca.prestamos.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
}