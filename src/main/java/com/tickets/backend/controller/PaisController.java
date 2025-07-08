package com.tickets.backend.controller;

import com.tickets.backend.dto.PaisDto;
import com.tickets.backend.models.Pais;
import com.tickets.backend.service.PaisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paises")
@CrossOrigin(origins = "*")
public class PaisController {

    @Autowired
    private PaisService paisService;

    @GetMapping
    public ResponseEntity<List<Pais>> obtenerTodos() {
        return ResponseEntity.ok(paisService.obtenerTodosPaises());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Pais>> obtenerActivos() {
        return ResponseEntity.ok(paisService.obtenerPaisesActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pais> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(paisService.obtenerPaisPorId(id));
    }

    @PostMapping
    public ResponseEntity<Pais> crear(@Valid @RequestBody PaisDto paisDto) {
        return new ResponseEntity<>(paisService.crearPais(paisDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pais> actualizar(@PathVariable Long id, @Valid @RequestBody PaisDto paisDto) {
        return ResponseEntity.ok(paisService.actualizarPais(id, paisDto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        paisService.desactivarPais(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        paisService.activarPais(id);
        return ResponseEntity.noContent().build();
    }
}