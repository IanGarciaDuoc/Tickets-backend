package com.tickets.backend.controller;

import com.tickets.backend.models.ConfiguracionSistema;
import com.tickets.backend.service.ConfiguracionSistemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionesController {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionesController.class);

    // Constantes para las claves de configuración
    private static final String NOTIF_MASTER_ENABLED = "NOTIF_MASTER_ENABLED";
    private static final String NOTIF_EMAIL_CREATION = "NOTIF_EMAIL_CREATION";
    private static final String NOTIF_EMAIL_ASSIGNMENT = "NOTIF_EMAIL_ASSIGNMENT";
    private static final String NOTIF_EMAIL_PROGRESS = "NOTIF_EMAIL_PROGRESS";
    private static final String NOTIF_EMAIL_RESOLUTION = "NOTIF_EMAIL_RESOLUTION";
    private static final String NOTIF_EMAIL_AUTO_CLOSE = "NOTIF_EMAIL_AUTO_CLOSE";
    private static final String NOTIF_EMAIL_CC_SUPERVISORS = "NOTIF_EMAIL_CC_SUPERVISORS";

    @Autowired
    private ConfiguracionSistemaService configuracionService;
    
    /**
     * Obtener todas las configuraciones de notificaciones
     */
    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, Boolean>> obtenerConfiguraciones() {
        try {
            logger.info("Obteniendo configuraciones de notificaciones");
            Map<String, Boolean> config = new HashMap<>();
            
            // Obtener todas las configuraciones con valores predeterminados (false)
            config.put(NOTIF_MASTER_ENABLED, configuracionService.obtenerValorBooleano(NOTIF_MASTER_ENABLED, false));
            config.put(NOTIF_EMAIL_CREATION, configuracionService.obtenerValorBooleano(NOTIF_EMAIL_CREATION, false));
            config.put(NOTIF_EMAIL_ASSIGNMENT, configuracionService.obtenerValorBooleano(NOTIF_EMAIL_ASSIGNMENT, false));
            config.put(NOTIF_EMAIL_PROGRESS, configuracionService.obtenerValorBooleano(NOTIF_EMAIL_PROGRESS, false));
            config.put(NOTIF_EMAIL_RESOLUTION, configuracionService.obtenerValorBooleano(NOTIF_EMAIL_RESOLUTION, false));
            config.put(NOTIF_EMAIL_AUTO_CLOSE, configuracionService.obtenerValorBooleano(NOTIF_EMAIL_AUTO_CLOSE, false));
            config.put(NOTIF_EMAIL_CC_SUPERVISORS, configuracionService.obtenerValorBooleano(NOTIF_EMAIL_CC_SUPERVISORS, false));
            
            logger.info("Configuraciones obtenidas: {}", config);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Error al obtener configuraciones: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Guardar todas las configuraciones de notificaciones
     */
    @PostMapping("/configuracion")
    public ResponseEntity<Boolean> guardarConfiguraciones(@RequestBody Map<String, Boolean> configuraciones) {
        try {
            logger.info("Guardando configuraciones: {}", configuraciones);
            
            List<ConfiguracionSistema> configList = configuraciones.entrySet().stream()
                .map(entry -> {
                    String clave = entry.getKey();
                    String descripcion = obtenerDescripcion(clave);
                    
                    ConfiguracionSistema config = configuracionService.obtenerPorClave(clave)
                        .orElse(new ConfiguracionSistema(clave, entry.getValue().toString(), descripcion));
                    
                    config.setValor(entry.getValue().toString());
                    return config;
                })
                .collect(Collectors.toList());
            
            configuracionService.guardarTodas(configList);
            logger.info("Configuraciones guardadas exitosamente");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("Error al guardar configuraciones: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    /**
     * NUEVO ENDPOINT: Actualizar valor de una configuración por clave
     * Este duplica la funcionalidad del ConfiguracionSistemaController
     * pero está dentro del contexto de notificaciones
     */
    @PatchMapping("/configuracion/{clave}")
    public ResponseEntity<ConfiguracionSistema> actualizarValorPorClave(
            @PathVariable String clave,
            @RequestBody Map<String, String> datos) {
        
        try {
            logger.info("Actualizando configuración {}: {}", clave, datos);
            
            String nuevoValor = datos.get("valor");
            if (nuevoValor == null) {
                logger.warn("Solicitud incorrecta: no se proporcionó valor");
                return ResponseEntity.badRequest().build();
            }
            
            return configuracionService.obtenerPorClave(clave)
                .map(configExistente -> {
                    configExistente.setValor(nuevoValor);
                    ConfiguracionSistema resultado = configuracionService.guardar(configExistente);
                    logger.info("Configuración {} actualizada exitosamente a {}", clave, nuevoValor);
                    return ResponseEntity.ok(resultado);
                })
                .orElseGet(() -> {
                    // Si no existe, la creamos
                    ConfiguracionSistema nuevaConfig = new ConfiguracionSistema(
                        clave, 
                        nuevoValor,
                        obtenerDescripcion(clave)
                    );
                    ConfiguracionSistema resultado = configuracionService.guardar(nuevaConfig);
                    logger.info("Nueva configuración {} creada con valor {}", clave, nuevoValor);
                    return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
                });
        } catch (Exception e) {
            logger.error("Error al actualizar configuración {}: ", clave, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener descripción para una clave de configuración
     */
    private String obtenerDescripcion(String clave) {
        Map<String, String> descripciones = new HashMap<>();
        descripciones.put(NOTIF_MASTER_ENABLED, "Habilitar/deshabilitar todas las notificaciones por correo electrónico");
        descripciones.put(NOTIF_EMAIL_CREATION, "Notificar cuando se crea un nuevo ticket");
        descripciones.put(NOTIF_EMAIL_ASSIGNMENT, "Notificar cuando se asigna un ticket a un técnico");
        descripciones.put(NOTIF_EMAIL_PROGRESS, "Notificar cuando un ticket pasa a estado En Progreso");
        descripciones.put(NOTIF_EMAIL_RESOLUTION, "Notificar cuando un ticket se marca como Resuelto");
        descripciones.put(NOTIF_EMAIL_AUTO_CLOSE, "Notificar cuando un ticket se cierra automáticamente");
        descripciones.put(NOTIF_EMAIL_CC_SUPERVISORS, "Incluir a los supervisores en copia de las notificaciones");
        
        return descripciones.getOrDefault(clave, "Configuración de notificaciones");
    }
    
    /**
     * Verificar si un tipo de notificación está habilitado
     * Método utilitario que puede ser usado por otros servicios
     */
    @GetMapping("/habilitada/{tipo}")
    public ResponseEntity<Boolean> isNotificacionHabilitada(@PathVariable String tipo) {
        try {
            // Si el switch principal está desactivado, ninguna notificación está habilitada
            if (!configuracionService.obtenerValorBooleano(NOTIF_MASTER_ENABLED, false)) {
                return ResponseEntity.ok(false);
            }
            
            // Si el switch principal está activado, verificamos el tipo específico
            return ResponseEntity.ok(configuracionService.obtenerValorBooleano(tipo, false));
        } catch (Exception e) {
            logger.error("Error al verificar si la notificación {} está habilitada: ", tipo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    /**
     * Inicializar configuraciones de notificaciones si no existen
     * Método que puede ser llamado al iniciar la aplicación
     */
    @PostMapping("/inicializar")
    public ResponseEntity<Boolean> inicializarConfiguraciones() {
        try {
            logger.info("Inicializando configuraciones de notificaciones");
            
            // Lista de todas las configuraciones de notificaciones
            Map<String, String> configuraciones = new HashMap<>();
            configuraciones.put(NOTIF_MASTER_ENABLED, "false");
            configuraciones.put(NOTIF_EMAIL_CREATION, "false");
            configuraciones.put(NOTIF_EMAIL_ASSIGNMENT, "false");
            configuraciones.put(NOTIF_EMAIL_PROGRESS, "false");
            configuraciones.put(NOTIF_EMAIL_RESOLUTION, "false");
            configuraciones.put(NOTIF_EMAIL_AUTO_CLOSE, "false");
            configuraciones.put(NOTIF_EMAIL_CC_SUPERVISORS, "false");
            
            // Para cada configuración, verificar si existe y crearla si no
            for (Map.Entry<String, String> entry : configuraciones.entrySet()) {
                String clave = entry.getKey();
                if (!configuracionService.obtenerPorClave(clave).isPresent()) {
                    ConfiguracionSistema config = new ConfiguracionSistema(
                        clave,
                        entry.getValue(),
                        obtenerDescripcion(clave)
                    );
                    configuracionService.guardar(config);
                    logger.info("Configuración {} inicializada", clave);
                }
            }
            
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            logger.error("Error al inicializar configuraciones: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}