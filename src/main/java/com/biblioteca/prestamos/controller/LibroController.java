package com.biblioteca.prestamos.controller;

import com.biblioteca.prestamos.dto.LibroRequestDto;
import com.biblioteca.prestamos.dto.LibroResponseDto;
import com.biblioteca.prestamos.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @PostMapping
    public ResponseEntity<LibroResponseDto> registrar(@Valid @RequestBody LibroRequestDto dto) {
        return new ResponseEntity<>(libroService.guardarLibro(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LibroResponseDto>> listar() {
        return ResponseEntity.ok(libroService.listarLibros());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibroResponseDto> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody LibroRequestDto dto) {
        return ResponseEntity.ok(libroService.actualizarLibro(id, dto));
    }

    // Desactivación lógica del libro
    @DeleteMapping("/{id}")
    public ResponseEntity<LibroResponseDto> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.desactivarLibro(id));
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<LibroResponseDto> activar(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.activarLibro(id));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<LibroResponseDto>> listarDisponibles() {
        return ResponseEntity.ok(libroService.listarDisponibles());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<LibroResponseDto>> buscar(@RequestParam String texto) {
        return ResponseEntity.ok(libroService.buscar(texto));
    }
}
