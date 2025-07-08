package com.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String nombre;
    private String apellido;
    private List<String> roles;
}