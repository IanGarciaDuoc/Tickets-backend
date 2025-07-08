// NotificacionCategoriaDto.java
package com.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionCategoriaDto {
    private Long id;
    private Long categoriaId;
    private String email;
    private String descripcion;
}