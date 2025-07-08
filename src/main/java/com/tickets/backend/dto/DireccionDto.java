package com.tickets.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DireccionDto {
    
    private Long id;
    
    @NotBlank(message = "La calle no puede estar vacía")
    private String calle;
    
    @NotBlank(message = "El número no puede estar vacío")
    private String numero;
    
    @NotNull(message = "La dirección debe pertenecer a una comuna")
    private Long comunaId;
    
    private boolean activo = true;
}