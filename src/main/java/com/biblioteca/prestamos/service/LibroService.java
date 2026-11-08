package com.biblioteca.prestamos.service;

import com.biblioteca.prestamos.dto.LibroRequestDto;
import com.biblioteca.prestamos.dto.LibroResponseDto;
import com.biblioteca.prestamos.entity.EstadoPrestamo;
import com.biblioteca.prestamos.entity.Libro;
import com.biblioteca.prestamos.exception.BadRequestException;
import com.biblioteca.prestamos.exception.BusinessRuleException;
import com.biblioteca.prestamos.exception.DuplicateResourceException;
import com.biblioteca.prestamos.exception.ResourceNotFoundException;
import com.biblioteca.prestamos.repository.LibroRepository;
import com.biblioteca.prestamos.repository.PrestamoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public LibroService(LibroRepository libroRepository, PrestamoRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional(readOnly = true)
    public List<LibroResponseDto> listarLibros() {
        return mapear(libroRepository.findAll(Sort.by("id")));
    }

    @Transactional(readOnly = true)
    public LibroResponseDto obtenerPorId(Long id) {
        Libro libro = buscarLibro(id);
        boolean prestado = prestamoRepository.existsByLibroIdAndEstado(id, EstadoPrestamo.ACTIVO);
        return toDto(libro, prestado);
    }

    public LibroResponseDto guardarLibro(LibroRequestDto dto) {
        String isbn = dto.isbn().trim();
        if (libroRepository.existsByIsbn(isbn)) {
            throw new DuplicateResourceException("Ya existe un libro registrado con el ISBN " + isbn + ".");
        }

        Libro libro = new Libro();
        aplicarDatos(libro, dto, isbn);
        Libro guardado = libroRepository.save(libro);
        return toDto(guardado, false);
    }

    public LibroResponseDto actualizarLibro(Long id, LibroRequestDto dto) {
        Libro libro = buscarLibro(id);
        String isbn = dto.isbn().trim();
        if (libroRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new DuplicateResourceException("Ya existe otro libro registrado con el ISBN " + isbn + ".");
        }

        aplicarDatos(libro, dto, isbn);
        Libro guardado = libroRepository.save(libro);
        boolean prestado = prestamoRepository.existsByLibroIdAndEstado(id, EstadoPrestamo.ACTIVO);
        return toDto(guardado, prestado);
    }

    /** "Eliminación" lógica: el libro se desactiva para conservar el historial de préstamos. */
    public LibroResponseDto desactivarLibro(Long id) {
        Libro libro = buscarLibro(id);
        if (!libro.isActivo()) {
            throw new BusinessRuleException("El libro '" + libro.getTitulo() + "' ya está desactivado.");
        }
        if (prestamoRepository.existsByLibroIdAndEstado(id, EstadoPrestamo.ACTIVO)) {
            throw new BusinessRuleException("No se puede desactivar el libro '" + libro.getTitulo()
                    + "' porque tiene un préstamo activo. Registre primero su devolución.");
        }
        libro.setActivo(false);
        return toDto(libroRepository.save(libro), false);
    }

    public LibroResponseDto activarLibro(Long id) {
        Libro libro = buscarLibro(id);
        if (libro.isActivo()) {
            throw new BusinessRuleException("El libro '" + libro.getTitulo() + "' ya está activo.");
        }
        libro.setActivo(true);
        return toDto(libroRepository.save(libro), false);
    }

    /** Filtro 1: libros activos que no tienen un préstamo activo. */
    @Transactional(readOnly = true)
    public List<LibroResponseDto> listarDisponibles() {
        return libroRepository.findDisponibles(EstadoPrestamo.ACTIVO).stream()
                .map(libro -> toDto(libro, false))
                .toList();
    }

    /** Filtro 2: búsqueda por texto en el título o el autor. */
    @Transactional(readOnly = true)
    public List<LibroResponseDto> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new BadRequestException("Debe indicar el texto a buscar.");
        }
        String criterio = texto.trim();
        return mapear(libroRepository
                .findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrderByIdAsc(criterio, criterio));
    }

    // ---------- métodos privados ----------

    private Libro buscarLibro(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El libro con ID " + id + " no existe."));
    }

    private void aplicarDatos(Libro libro, LibroRequestDto dto, String isbn) {
        libro.setTitulo(dto.titulo().trim());
        libro.setAutor(dto.autor().trim());
        libro.setIsbn(isbn);
    }

    private List<LibroResponseDto> mapear(List<Libro> libros) {
        Set<Long> prestados = new HashSet<>(prestamoRepository.findLibroIdsByEstado(EstadoPrestamo.ACTIVO));
        return libros.stream()
                .map(libro -> toDto(libro, prestados.contains(libro.getId())))
                .toList();
    }

    private LibroResponseDto toDto(Libro libro, boolean prestado) {
        return new LibroResponseDto(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getIsbn(),
                libro.isActivo(),
                libro.isActivo() && !prestado);
    }
}
