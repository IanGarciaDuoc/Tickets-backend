package com.tickets.backend.controller;

import com.tickets.backend.models.Etiqueta;
import com.tickets.backend.service.EtiquetaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Ajusta esto según tus políticas de seguridad
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    
    public EtiquetaController(EtiquetaService etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    @GetMapping("/etiquetas")
    public ResponseEntity<List<Etiqueta>> obtenerEtiquetas() {
        List<Etiqueta> etiquetas = etiquetaService.obtenerTodasLasEtiquetas();
        return ResponseEntity.ok(etiquetas);
    }

    @GetMapping("/etiquetas/{id}")
    public ResponseEntity<Etiqueta> obtenerEtiquetaPorId(@PathVariable Long id) {
        return etiquetaService.obtenerEtiquetaPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/etiquetas/nombre/{nombre}")
    public ResponseEntity<Etiqueta> obtenerEtiquetaPorNombre(@PathVariable String nombre) {
        return etiquetaService.buscarPorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/etiquetas/buscar")
    public ResponseEntity<List<Etiqueta>> buscarEtiquetas(@RequestParam String keyword) {
        List<Etiqueta> etiquetas = etiquetaService.buscarPorPalabraClave(keyword);
        return ResponseEntity.ok(etiquetas);
    }

    @GetMapping("/tickets/{ticketId}/etiquetas")
    public ResponseEntity<List<Etiqueta>> obtenerEtiquetasPorTicket(@PathVariable Long ticketId) {
        List<Etiqueta> etiquetas = etiquetaService.obtenerEtiquetasPorTicket(ticketId);
        return ResponseEntity.ok(etiquetas);
    }

    @PostMapping("/etiquetas")
    public ResponseEntity<Etiqueta> crearEtiqueta(@RequestBody Etiqueta etiqueta) {
        if (etiquetaService.existePorNombre(etiqueta.getNombre())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Etiqueta nuevaEtiqueta = etiquetaService.guardarEtiqueta(etiqueta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaEtiqueta);
    }

    @PutMapping("/etiquetas/{id}")
    public ResponseEntity<Etiqueta> actualizarEtiqueta(@PathVariable Long id, @RequestBody Etiqueta etiqueta) {
        return etiquetaService.obtenerEtiquetaPorId(id)
            .map(etiquetaExistente -> {
                // Verificar si el nuevo nombre ya existe en otra etiqueta
                if (!etiquetaExistente.getNombre().equals(etiqueta.getNombre()) && 
                    etiquetaService.existePorNombre(etiqueta.getNombre())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).<Etiqueta>build();
                }
                
                etiqueta.setId(id);
                // Mantener la lista de tickets
                etiqueta.setTickets(etiquetaExistente.getTickets());
                return ResponseEntity.ok(etiquetaService.guardarEtiqueta(etiqueta));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/etiquetas/{id}")
    public ResponseEntity<Void> eliminarEtiqueta(@PathVariable Long id) {
        return etiquetaService.obtenerEtiquetaPorId(id)
            .map(etiqueta -> {
                etiquetaService.eliminarEtiqueta(id);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}