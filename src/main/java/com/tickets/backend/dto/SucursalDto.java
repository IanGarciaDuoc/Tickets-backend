package com.tickets.backend.dto;

import com.tickets.backend.models.Direccion;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    private String nombre;
    
    private String telefono;
    
    // Para asociar con una dirección existente
    private Long direccionId;
    
    // Para crear una nueva dirección
    private Direccion direccion;
    
    private boolean activo = true;
}