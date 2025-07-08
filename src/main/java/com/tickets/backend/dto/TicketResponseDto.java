// src/main/java/com/tickets/backend/dto/TicketResponseDto.java
package com.tickets.backend.dto;

import com.tickets.backend.models.EstadoTicket;
import com.tickets.backend.models.PrioridadTicket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {
    
    private Long id;
    private String numeroTicket;
    private String titulo;
    private String descripcion;
    private EstadoTicket estado;
    private PrioridadTicket prioridad;
    
    // Solo IDs para evitar ciclos
    private Long categoriaId;
    private String categoriaNombre;
    
    private Long subcategoriaId;
    private String subcategoriaNombre;
    
    // Usuario creador - Añadiendo campos separados
    private Long usuarioCreadorId;
    private String usuarioCreadorNombre;
    // Nuevos campos separados para compatibilidad con frontend
    private String usuarioNombre;
    private String usuarioApellido;
    
    // Técnico asignado - Añadiendo campos separados
    private Long tecnicoAsignadoId;
    private String tecnicoAsignadoNombre;
    // Nuevos campos separados para compatibilidad con frontend
    private String tecnicoAsignadoNombreFirstOnly;
    private String tecnicoAsignadoApellido;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaCierre;
}