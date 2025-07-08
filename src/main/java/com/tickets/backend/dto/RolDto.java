package com.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Set<Long> permisosIds;
}