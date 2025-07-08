package com.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermisoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private String modulo;
    private String accion;
}