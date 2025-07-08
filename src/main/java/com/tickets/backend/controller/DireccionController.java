package com.tickets.backend.controller;

import com.tickets.backend.models.Direccion;
import com.tickets.backend.service.DireccionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/direcciones")
@CrossOrigin(origins = "*")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    @GetMapping
    public ResponseEntity<List<Direccion>> obtenerTodas() {
        return ResponseEntity.ok(direccionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Direccion> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(direccionService.obtenerPorId(id));
    }

    @GetMapping("/comuna/{comunaId}")
    public ResponseEntity<List<Direccion>> obtenerPorComuna(@PathVariable Long comunaId) {
        return ResponseEntity.ok(direccionService.obtenerPorComunaId(comunaId));
    }

    @PostMapping
    public ResponseEntity<Direccion> crear(@Valid @RequestBody Direccion direccion) {
        return new ResponseEntity<>(direccionService.guardar(direccion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Direccion> actualizar(@PathVariable Long id, @Valid @RequestBody Direccion direccion) {
        return ResponseEntity.ok(direccionService.actualizar(id, direccion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        direccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar/calle")
    public ResponseEntity<List<Direccion>> buscarPorCalle(@RequestParam String calle) {
        return ResponseEntity.ok(direccionService.buscarPorCalle(calle));
    }
}