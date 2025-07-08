package com.tickets.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tickets.backend.models.ConfiguracionCorreo;
import com.tickets.backend.service.ConfiguracionCorreoService;
import com.tickets.backend.service.EmailService;

@RestController
@RequestMapping("/api/configuraciones-correo")
public class ConfiguracionCorreoController {

    @Autowired
    private ConfiguracionCorreoService configuracionCorreoService;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Obtener todas las configuraciones de correo
     */
    @GetMapping
    public ResponseEntity<List<ConfiguracionCorreo>> obtenerTodasLasConfiguraciones() {
        return ResponseEntity.ok(configuracionCorreoService.obtenerTodas());
    }
    
    /**
     * Obtener configuración por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracionCorreo> obtenerPorId(@PathVariable Long id) {
        return configuracionCorreoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtener la configuración activa
     */
    @GetMapping("/activa")
    public ResponseEntity<ConfiguracionCorreo> obtenerConfiguracionActiva() {
        return configuracionCorreoService.obtenerConfiguracionActiva()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Crear una nueva configuración
     */
    @PostMapping
    public ResponseEntity<ConfiguracionCorreo> crearConfiguracion(@RequestBody ConfiguracionCorreo configuracion) {
        ConfiguracionCorreo configuracionGuardada = configuracionCorreoService.guardar(configuracion);
        
        // Si la configuración es activa, actualizar el MailSender
        if (Boolean.TRUE.equals(configuracion.getActivo())) {
            emailService.actualizarMailSender();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configuracionGuardada);
    }
    
    /**
     * Actualizar una configuración existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracionCorreo> actualizarConfiguracion(
            @PathVariable Long id,
            @RequestBody ConfiguracionCorreo configuracion) {
        
        return configuracionCorreoService.obtenerPorId(id)
                .map(configExistente -> {
                    configuracion.setId(id);
                    ConfiguracionCorreo configuracionActualizada = configuracionCorreoService.guardar(configuracion);
                    
                    // Si la configuración es activa, actualizar el MailSender
                    if (Boolean.TRUE.equals(configuracion.getActivo())) {
                        emailService.actualizarMailSender();
                    }
                    
                    return ResponseEntity.ok(configuracionActualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Activar una configuración específica
     */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Boolean> activarConfiguracion(@PathVariable Long id) {
        boolean resultado = configuracionCorreoService.activarConfiguracion(id);
        
        if (resultado) {
            // Actualizar el MailSender con la nueva configuración activa
            emailService.actualizarMailSender();
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Eliminar una configuración
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarConfiguracion(@PathVariable Long id) {
        return configuracionCorreoService.obtenerPorId(id)
                .map(configExistente -> {
                    boolean eraActiva = Boolean.TRUE.equals(configExistente.getActivo());
                    configuracionCorreoService.eliminar(id);
                    
                    // Si la configuración eliminada era la activa, actualizar el MailSender
                    if (eraActiva) {
                        emailService.actualizarMailSender();
                    }
                    
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Endpoint para probar la configuración de correo
     */
    @PostMapping("/probar")
    public ResponseEntity<Boolean> probarConfiguracion(
            @RequestBody ConfiguracionCorreo configuracion, 
            @RequestParam String emailDestino) {
        
        try {
            boolean resultado = emailService.probarConfiguracion(configuracion, emailDestino);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}