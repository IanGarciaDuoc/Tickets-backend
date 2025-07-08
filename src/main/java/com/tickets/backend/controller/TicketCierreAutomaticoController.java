package com.tickets.backend.controller;

import com.tickets.backend.service.TicketCierreAutomaticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets/cierre-automatico")
@CrossOrigin(origins = "*")
public class TicketCierreAutomaticoController {

    @Autowired
    private TicketCierreAutomaticoService cierreAutomaticoService;

    /**
     * Ejecuta el proceso de cierre automático manualmente
     */
    @PostMapping("/ejecutar")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Map<String, Object>> ejecutarCierreAutomatico() {
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Cambiar para usar el método que cierra TODOS los tickets resueltos
        cierreAutomaticoService.ejecutarCierreAutomaticoManualCompleto();
        response.put("mensaje", "Proceso de cierre automático ejecutado correctamente - Todos los tickets resueltos han sido cerrados");
        response.put("estado", "success");
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        response.put("mensaje", "Error al ejecutar el cierre automático: " + e.getMessage());
        response.put("estado", "error");
        return ResponseEntity.status(500).body(response);
    }
}
@PostMapping("/ejecutar-programado")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Map<String, Object>> ejecutarCierreProgramado() {
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Este usa la lógica original con filtro de días
        cierreAutomaticoService.ejecutarCierreAutomaticoManual();
        response.put("mensaje", "Proceso programado ejecutado - Solo tickets que cumplan el criterio de días");
        response.put("estado", "success");
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        response.put("mensaje", "Error al ejecutar el cierre programado: " + e.getMessage());
        response.put("estado", "error");
        return ResponseEntity.status(500).body(response);
    }
}

    /**
     * Obtiene información sobre el estado del cierre automático
     */
    @GetMapping("/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadoCierreAutomatico() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String informacion = cierreAutomaticoService.obtenerInformacionProximoCierre();
            response.put("informacion", informacion);
            response.put("estado", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al obtener información: " + e.getMessage());
            response.put("estado", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Actualiza la configuración del scheduler
     */
    @PostMapping("/actualizar-scheduler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizarScheduler() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            cierreAutomaticoService.actualizarScheduler();
            response.put("mensaje", "Scheduler actualizado correctamente");
            response.put("estado", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al actualizar scheduler: " + e.getMessage());
            response.put("estado", "error");
            return ResponseEntity.status(500).body(response);
        }
    }
}