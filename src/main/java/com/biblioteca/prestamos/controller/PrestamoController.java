package com.biblioteca.prestamos.controller;

import com.biblioteca.prestamos.dto.IndicadoresDto;
import com.biblioteca.prestamos.dto.PrestamoRequestDto;
import com.biblioteca.prestamos.dto.PrestamoResponseDto;
import com.biblioteca.prestamos.dto.PrestamoUpdateDto;
import com.biblioteca.prestamos.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<PrestamoResponseDto> crearPrestamo(@Valid @RequestBody PrestamoRequestDto dto) {
        return new ResponseEntity<>(prestamoService.crearPrestamo(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponseDto>> listarPrestamos() {
        return ResponseEntity.ok(prestamoService.listarPrestamos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrestamoResponseDto> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody PrestamoUpdateDto dto) {
        return ResponseEntity.ok(prestamoService.actualizarPrestamo(id, dto));
    }

    @PutMapping("/devolver/{id}")
    public ResponseEntity<PrestamoResponseDto> devolverLibro(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.devolverLibro(id));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<PrestamoResponseDto>> listarPrestamosActivos() {
        return ResponseEntity.ok(prestamoService.listarPrestamosActivos());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<PrestamoResponseDto>> listarPrestamosVencidos() {
        return ResponseEntity.ok(prestamoService.listarPrestamosVencidos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PrestamoResponseDto>> buscarPorLector(@RequestParam String lector) {
        return ResponseEntity.ok(prestamoService.buscarPorLector(lector));
    }

    @GetMapping("/indicadores")
    public ResponseEntity<IndicadoresDto> obtenerIndicadores() {
        return ResponseEntity.ok(prestamoService.obtenerIndicadores());
    }
}
