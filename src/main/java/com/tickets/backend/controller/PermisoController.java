package com.tickets.backend.controller;

import com.tickets.backend.models.Permiso;
import com.tickets.backend.service.PermisoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@CrossOrigin(origins = "*")
public class PermisoController {

    @Autowired
    private PermisoService permisoService;

    @GetMapping
    @PreAuthorize("hasAuthority('LEER_PERMISOS')")
    public ResponseEntity<List<Permiso>> obtenerTodosPermisos() {
        return ResponseEntity.ok(permisoService.obtenerTodosPermisos());
    }
    
    @GetMapping("/modulo/{modulo}")
    @PreAuthorize("hasAuthority('LEER_PERMISOS')")
    public ResponseEntity<List<Permiso>> obtenerPermisosPorModulo(@PathVariable String modulo) {
        return ResponseEntity.ok(permisoService.obtenerPermisosPorModulo(modulo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEER_PERMISOS')")
    public ResponseEntity<Permiso> obtenerPermisoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(permisoService.obtenerPermisoPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREAR_PERMISOS')")
    public ResponseEntity<Permiso> crearPermiso(@Valid @RequestBody Permiso permiso) {
        return new ResponseEntity<>(permisoService.crearPermiso(permiso), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTUALIZAR_PERMISOS')")
    public ResponseEntity<Permiso> actualizarPermiso(@PathVariable Long id, @Valid @RequestBody Permiso permiso) {
        return ResponseEntity.ok(permisoService.actualizarPermiso(id, permiso));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ELIMINAR_PERMISOS')")
    public ResponseEntity<Void> eliminarPermiso(@PathVariable Long id) {
        permisoService.eliminarPermiso(id);
        return ResponseEntity.noContent().build();
    }
}