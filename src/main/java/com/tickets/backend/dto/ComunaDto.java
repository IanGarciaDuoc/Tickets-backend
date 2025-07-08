package com.tickets.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComunaDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre de la comuna no puede estar vacío")
    private String nombre;
    
    private String codigo;
    
    @NotNull(message = "La comuna debe pertenecer a una región")
    private Long regionId;
    
    private boolean activo = true;
}