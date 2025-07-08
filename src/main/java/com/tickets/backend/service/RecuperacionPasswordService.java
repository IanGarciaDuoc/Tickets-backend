package com.tickets.backend.service;

import com.tickets.backend.exceptions.ResourceNotFoundException;
import com.tickets.backend.models.PasswordResetToken;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.PasswordResetTokenRepository;
import com.tickets.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecuperacionPasswordService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Transactional
    public void solicitarRecuperacionPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        
        // Verificar si ya existe un token para este usuario y eliminarlo
        tokenRepository.findByUsuario(usuario).ifPresent(tokenRepository::delete);
        
        // Generar token único
        String token = UUID.randomUUID().toString();
        
        // Crear y guardar el token con expiración (24 horas)
        PasswordResetToken resetToken = new PasswordResetToken(
            token, 
            usuario, 
            LocalDateTime.now().plusHours(24)
        );
        tokenRepository.save(resetToken);
        
        // Enviar correo
        emailService.enviarCorreoRecuperacionPassword(email, token);
    }
    
    @Transactional
    public boolean resetearPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido"));
        
        if (resetToken.isExpirado()) {
            tokenRepository.delete(resetToken);
            return false; // Token expirado
        }
        
        Usuario usuario = resetToken.getUsuario();
        
        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        
        // Eliminar token usado
        tokenRepository.delete(resetToken);
        
        return true;
    }
    public boolean validarToken(String token) {
        try {
            PasswordResetToken resetToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Token inválido"));
            
            return !resetToken.isExpirado();
        } catch (Exception e) {
            return false;
        }
    }
}