package com.tickets.backend.controller;

import com.tickets.backend.dto.ComunaDto;
import com.tickets.backend.models.Comuna;
import com.tickets.backend.service.ComunaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comunas")
@CrossOrigin(origins = "*")
public class ComunaController {

    @Autowired
    private ComunaService comunaService;

    @GetMapping
    public ResponseEntity<List<Comuna>> obtenerTodas() {
        return ResponseEntity.ok(comunaService.obtenerTodasComunas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Comuna>> obtenerActivas() {
        return ResponseEntity.ok(comunaService.obtenerComunasActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comuna> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comunaService.obtenerComunaPorId(id));
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<Comuna>> obtenerPorRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(comunaService.obtenerComunasPorRegion(regionId));
    }

    @GetMapping("/region/{regionId}/activas")
    public ResponseEntity<List<Comuna>> obtenerActivasPorRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(comunaService.obtenerComunasActivasPorRegion(regionId));
    }

    @PostMapping
    public ResponseEntity<Comuna> crear(@Valid @RequestBody ComunaDto comunaDto) {
        return new ResponseEntity<>(comunaService.crearComuna(comunaDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comuna> actualizar(@PathVariable Long id, @Valid @RequestBody ComunaDto comunaDto) {
        return ResponseEntity.ok(comunaService.actualizarComuna(id, comunaDto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        comunaService.desactivarComuna(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        comunaService.activarComuna(id);
        return ResponseEntity.noContent().build();
    }
}