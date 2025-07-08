package com.tickets.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaisDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre del país no puede estar vacío")
    private String nombre;
    
    private String codigo;
    
    private boolean activo = true;
}