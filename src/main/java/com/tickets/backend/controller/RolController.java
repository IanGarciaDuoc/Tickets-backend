package com.tickets.backend.controller;

import com.tickets.backend.models.Rol;
import com.tickets.backend.service.RolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping
    @PreAuthorize("hasAuthority('LEER_ROLES')")
    public ResponseEntity<List<Rol>> obtenerTodosRoles() {
        return ResponseEntity.ok(rolService.obtenerTodosRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEER_ROLES')")
    public ResponseEntity<Rol> obtenerRolPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerRolPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREAR_ROLES')")
    public ResponseEntity<Rol> crearRol(@Valid @RequestBody Rol rol) {
        return new ResponseEntity<>(rolService.crearRol(rol), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTUALIZAR_ROLES')")
    public ResponseEntity<Rol> actualizarRol(@PathVariable Long id, @Valid @RequestBody Rol rol) {
        return ResponseEntity.ok(rolService.actualizarRol(id, rol));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ELIMINAR_ROLES')")
    public ResponseEntity<Void> eliminarRol(@PathVariable Long id) {
        rolService.eliminarRol(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permisos")
    @PreAuthorize("hasAuthority('GESTIONAR_PERMISOS_ROLES')")
    public ResponseEntity<Rol> asignarPermisosARol(
            @PathVariable Long id,
            @RequestBody Set<Long> permisosIds) {
        return ResponseEntity.ok(rolService.asignarPermisosARol(id, permisosIds));
    }
}