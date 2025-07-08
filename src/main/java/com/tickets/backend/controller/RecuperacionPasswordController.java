package com.tickets.backend.controller;

import com.tickets.backend.dto.MensajeResponse;
import com.tickets.backend.dto.RecuperacionPasswordRequest;
import com.tickets.backend.dto.ResetPasswordRequest;
import com.tickets.backend.service.RecuperacionPasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "*")
public class RecuperacionPasswordController {

    @Autowired
    private RecuperacionPasswordService recuperacionPasswordService;

    @PostMapping("/recuperar")
    public ResponseEntity<MensajeResponse> solicitarRecuperacion(@Valid @RequestBody RecuperacionPasswordRequest request) {
        recuperacionPasswordService.solicitarRecuperacionPassword(request.getEmail());
        return ResponseEntity.ok(new MensajeResponse("Se ha enviado un correo con instrucciones para recuperar tu contraseña"));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetearPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean resultado = recuperacionPasswordService.resetearPassword(
                request.getToken(), 
                request.getNuevaPassword()
        );
        
        if (resultado) {
            return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente"));
        } else {
            return ResponseEntity.badRequest().body(new MensajeResponse("El enlace para restablecer la contraseña ha expirado o es inválido"));
        }
    }
    
    @GetMapping("/validar-token")
    public ResponseEntity<?> validarToken(@RequestParam String token) {
        boolean esValido = recuperacionPasswordService.validarToken(token);
        
        if (esValido) {
            return ResponseEntity.ok(new MensajeResponse("Token válido"));
        } else {
            return ResponseEntity.badRequest().body(new MensajeResponse("Token inválido o expirado"));
        }
    }
}