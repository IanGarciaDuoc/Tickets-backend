package com.tickets.backend.controller;

import com.tickets.backend.models.SupervisorTecnico;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.service.SupervisorTecnicoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervisor-tecnico")
@CrossOrigin(origins = "*")
public class SupervisorTecnicoController {
    
    @Autowired
    private SupervisorTecnicoService supervisorTecnicoService;
    
    /**
     * Asignar un técnico a un supervisor
     */
    @PostMapping("/asignar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<SupervisorTecnico> asignarTecnicoASupervisor(
            @RequestBody AsignacionRequest request) {
        SupervisorTecnico relacion = supervisorTecnicoService.asignarTecnicoASupervisor(
                request.getSupervisorId(), request.getTecnicoId());
        return ResponseEntity.ok(relacion);
    }
    
    /**
     * Remover la asignación de un técnico de un supervisor
     */
    @DeleteMapping("/remover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Void> removerTecnicoDeSupervisor(
            @RequestParam Long supervisorId,
            @RequestParam Long tecnicoId) {
        supervisorTecnicoService.removerTecnicoDeSupervisor(supervisorId, tecnicoId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Obtener todos los técnicos de un supervisor
     */
    @GetMapping("/tecnicos/{supervisorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<Usuario>> obtenerTecnicosDeSupervisor(@PathVariable Long supervisorId) {
        List<Usuario> tecnicos = supervisorTecnicoService.obtenerTecnicosDeSupervisor(supervisorId);
        return ResponseEntity.ok(tecnicos);
    }
    
    /**
     * Obtener todos los supervisores de un técnico
     */
    @GetMapping("/supervisores/{tecnicoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('TECNICO')")
    public ResponseEntity<List<Usuario>> obtenerSupervisoresDeTecnico(@PathVariable Long tecnicoId) {
        List<Usuario> supervisores = supervisorTecnicoService.obtenerSupervisoresDeTecnico(tecnicoId);
        return ResponseEntity.ok(supervisores);
    }
    
    /**
     * Obtener técnicos disponibles para asignación según usuario actual
     */
    @GetMapping("/tecnicos-disponibles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('TECNICO')")
    public ResponseEntity<List<Usuario>> obtenerTecnicosDisponibles(
            @RequestParam Long usuarioId,
            @RequestParam Long categoriaId) {
        List<Usuario> tecnicos = supervisorTecnicoService.obtenerTecnicosDisponibles(usuarioId, categoriaId);
        return ResponseEntity.ok(tecnicos);
    }
    
    /**
     * Verificar si un usuario puede asignar tickets a otro usuario
     */
    @GetMapping("/puede-asignar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('TECNICO')")
    public ResponseEntity<Map<String, Boolean>> puedeAsignarTicket(
            @RequestParam Long asignadorId,
            @RequestParam Long tecnicoId) {
        boolean puede = supervisorTecnicoService.puedeAsignarTicket(asignadorId, tecnicoId);
        return ResponseEntity.ok(Map.of("puedeAsignar", puede));
    }
    
    // DTO para request
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class AsignacionRequest {
        private Long supervisorId;
        private Long tecnicoId;
    }
}