package com.tickets.backend.dto;

import com.tickets.backend.models.EstadoTicket;
import com.tickets.backend.models.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    
    private Long id;
    
    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
    private String titulo;
    
    @NotBlank(message = "La descripción no puede estar vacía")
    private String descripcion;
    
    private EstadoTicket estado;
    
    private PrioridadTicket prioridad;
    
    @NotNull(message = "La categoría no puede estar vacía")
    private Long categoriaId;
    
    @NotNull(message = "La subcategoría no puede estar vacía")  // ← VALIDACIÓN AGREGADA
    private Long subcategoriaId;
    
    private Long tecnicoAsignadoId;
    
    private List<Long> etiquetasIds;
}