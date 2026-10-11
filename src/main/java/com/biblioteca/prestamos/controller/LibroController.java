package com.biblioteca.prestamos.controller;

import com.biblioteca.prestamos.entity.Libro;
import com.biblioteca.prestamos.repository.LibroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroRepository libroRepository;

    public LibroController(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    // Consultar todos los libros
    @GetMapping
    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    // Registrar un libro
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro registrar(@RequestBody Libro libro) {
        return libroRepository.save(libro);
    }
}