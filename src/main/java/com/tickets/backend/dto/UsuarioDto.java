package com.tickets.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    private String password;
    
    private String especialidad;
    
    private boolean activo = true;
    
    private Long empresaId;
    
    private Long sucursalId;
    
    // NUEVO CAMPO
    private Long categoriaId;
    
    // Campo para IDs de roles (para compatibilidad con código existente)
    private Set<Long> rolesId;
    
    // NUEVO: Campo para nombres de roles (para el frontend)
    private List<String> roles;
}