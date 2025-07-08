// NotificacionCategoriaController.java
package com.tickets.backend.controller;

import com.tickets.backend.dto.NotificacionCategoriaDto;
import com.tickets.backend.models.NotificacionCategoria;
import com.tickets.backend.service.NotificacionCategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificacionCategoriaController {

    @Autowired
    private NotificacionCategoriaService notificacionCategoriaService;
    
    @GetMapping("/categorias/{categoriaId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<NotificacionCategoria>> obtenerNotificacionesPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(notificacionCategoriaService.obtenerNotificacionesPorCategoria(categoriaId));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<NotificacionCategoria> agregarNotificacion(@RequestBody NotificacionCategoriaDto dto) {
        return ResponseEntity.ok(notificacionCategoriaService.agregarNotificacion(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<NotificacionCategoria> actualizarNotificacion(@PathVariable Long id, @RequestBody NotificacionCategoriaDto dto) {
        return ResponseEntity.ok(notificacionCategoriaService.actualizarNotificacion(id, dto));
    }
    
    @PutMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> desactivarNotificacion(@PathVariable Long id) {
        notificacionCategoriaService.desactivarNotificacion(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionCategoriaService.eliminarNotificacion(id);
        return ResponseEntity.ok().build();
    }
}