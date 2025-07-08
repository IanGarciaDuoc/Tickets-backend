package com.tickets.backend.controller;

import com.tickets.backend.dto.JwtAuthResponse;
import com.tickets.backend.dto.LoginRequest;
import com.tickets.backend.dto.RegistroRequest;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Usar el servicio de autenticación que ya tiene la lógica implementada
        JwtAuthResponse jwtAuthResponse = authService.autenticar(loginRequest);
        return ResponseEntity.ok(jwtAuthResponse);
    }

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registro(@Valid @RequestBody RegistroRequest registroRequest) {
        Usuario nuevoUsuario = authService.registrarUsuario(registroRequest);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }
}