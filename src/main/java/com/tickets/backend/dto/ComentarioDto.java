package com.tickets.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDto {
    
    private Long id;
    
    @NotBlank(message = "El contenido no puede estar vacío")
    private String contenido;
    
    private boolean esPrivado;
}