package com.tickets.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Con esta anotación de Lombok no necesitarías escribir getters y setters
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponseDto {
    private Long id;
    private String nombre;
    private String codigo;
    private boolean activo;
    private PaisDto pais;
    private List<ComunaDto> comunas;
    
    
    
}