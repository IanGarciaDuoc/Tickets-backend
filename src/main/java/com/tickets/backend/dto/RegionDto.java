package com.tickets.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre de la región no puede estar vacío")
    private String nombre;
    
    private String codigo;
    
    @NotNull(message = "La región debe pertenecer a un país")
    private Long paisId;
    
    private boolean activo = true;
}