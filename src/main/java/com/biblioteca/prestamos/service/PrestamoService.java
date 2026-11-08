package com.biblioteca.prestamos.service;

import com.biblioteca.prestamos.dto.IndicadoresDto;
import com.biblioteca.prestamos.dto.PrestamoRequestDto;
import com.biblioteca.prestamos.dto.PrestamoResponseDto;
import com.biblioteca.prestamos.dto.PrestamoUpdateDto;
import com.biblioteca.prestamos.entity.EstadoPrestamo;
import com.biblioteca.prestamos.entity.Libro;
import com.biblioteca.prestamos.entity.Prestamo;
import com.biblioteca.prestamos.exception.BadRequestException;
import com.biblioteca.prestamos.exception.BusinessRuleException;
import com.biblioteca.prestamos.exception.ResourceNotFoundException;
import com.biblioteca.prestamos.repository.LibroRepository;
import com.biblioteca.prestamos.repository.PrestamoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;

    public PrestamoService(PrestamoRepository prestamoRepository, LibroRepository libroRepository) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
    }

    /**
     * Operación de negocio principal: registrar un préstamo.
     * Reglas: el libro debe existir, estar activo y no tener otro préstamo activo.
     */
    public PrestamoResponseDto crearPrestamo(PrestamoRequestDto dto) {
        Libro libro = libroRepository.findById(dto.libroId())
                .orElseThrow(() -> new ResourceNotFoundException("El libro con ID " + dto.libroId() + " no existe."));

        if (!libro.isActivo()) {
            throw new BusinessRuleException("El libro '" + libro.getTitulo()
                    + "' está desactivado y no puede prestarse.");
        }
        if (prestamoRepository.existsByLibroIdAndEstado(libro.getId(), EstadoPrestamo.ACTIVO)) {
            throw new BusinessRuleException("El libro '" + libro.getTitulo() + "' ya tiene un préstamo activo.");
        }

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setNombreLector(dto.nombreLector().trim());
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaDevolucionPrevista(hoy.plusDays(dto.diasPlazo()));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);

        return toDto(prestamoRepository.save(prestamo));
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDto> listarPrestamos() {
        return prestamoRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponseDto obtenerPorId(Long id) {
        return toDto(buscarPrestamo(id));
    }

    /** Modifica el lector o el plazo de un préstamo que sigue activo. */
    public PrestamoResponseDto actualizarPrestamo(Long id, PrestamoUpdateDto dto) {
        Prestamo prestamo = buscarPrestamo(id);
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessRuleException("No se puede modificar un préstamo que ya fue devuelto.");
        }
        prestamo.setNombreLector(dto.nombreLector().trim());
        prestamo.setFechaDevolucionPrevista(prestamo.getFechaPrestamo().plusDays(dto.diasPlazo()));
        return toDto(prestamoRepository.save(prestamo));
    }

    /** Cambia el estado del préstamo a DEVUELTO y registra la fecha real de devolución. */
    public PrestamoResponseDto devolverLibro(Long id) {
        Prestamo prestamo = buscarPrestamo(id);
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessRuleException("Este préstamo ya fue devuelto anteriormente.");
        }
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDate.now());
        return toDto(prestamoRepository.save(prestamo));
    }

    /** Filtro 1: préstamos activos. */
    @Transactional(readOnly = true)
    public List<PrestamoResponseDto> listarPrestamosActivos() {
        return prestamoRepository.findByEstadoOrderByIdDesc(EstadoPrestamo.ACTIVO).stream()
                .map(this::toDto)
                .toList();
    }

    /** Filtro 2: préstamos vencidos (activos con fecha prevista anterior a hoy). */
    @Transactional(readOnly = true)
    public List<PrestamoResponseDto> listarPrestamosVencidos() {
        return prestamoRepository
                .findByEstadoAndFechaDevolucionPrevistaBeforeOrderByFechaDevolucionPrevistaAsc(
                        EstadoPrestamo.ACTIVO, LocalDate.now())
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** Filtro 3: préstamos por nombre del lector. */
    @Transactional(readOnly = true)
    public List<PrestamoResponseDto> buscarPorLector(String lector) {
        if (lector == null || lector.isBlank()) {
            throw new BadRequestException("Debe indicar el nombre del lector a buscar.");
        }
        return prestamoRepository.findByNombreLectorContainingIgnoreCaseOrderByIdDesc(lector.trim()).stream()
                .map(this::toDto)
                .toList();
    }

    /** Indicador: resumen de libros y préstamos. */
    @Transactional(readOnly = true)
    public IndicadoresDto obtenerIndicadores() {
        long totalLibros = libroRepository.count();
        long librosActivos = libroRepository.countByActivoTrue();
        long disponibles = libroRepository.findDisponibles(EstadoPrestamo.ACTIVO).size();
        long prestados = Math.max(0, librosActivos - disponibles);
        double porcentaje = librosActivos == 0 ? 0.0 : Math.round(prestados * 1000.0 / librosActivos) / 10.0;

        long activos = prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO);
        long devueltos = prestamoRepository.countByEstado(EstadoPrestamo.DEVUELTO);
        long vencidos = prestamoRepository.countByEstadoAndFechaDevolucionPrevistaBefore(
                EstadoPrestamo.ACTIVO, LocalDate.now());

        return new IndicadoresDto(totalLibros, librosActivos, disponibles, prestados, porcentaje,
                activos + devueltos, activos, devueltos, vencidos);
    }

    // ---------- métodos privados ----------

    private Prestamo buscarPrestamo(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El préstamo con ID " + id + " no existe."));
    }

    private PrestamoResponseDto toDto(Prestamo prestamo) {
        boolean vencido = prestamo.getEstado() == EstadoPrestamo.ACTIVO
                && prestamo.getFechaDevolucionPrevista().isBefore(LocalDate.now());
        return new PrestamoResponseDto(
                prestamo.getId(),
                prestamo.getLibro().getId(),
                prestamo.getLibro().getTitulo(),
                prestamo.getLibro().getIsbn(),
                prestamo.getNombreLector(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaDevolucionPrevista(),
                prestamo.getFechaDevolucionReal(),
                prestamo.getEstado(),
                vencido);
    }
}
