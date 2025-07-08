package com.tickets.backend.service;

import com.tickets.backend.models.ConfiguracionSistema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificacionesService {

    @Autowired
    private ConfiguracionSistemaService configService;
    
    // Constantes para las claves de configuración
    public static final String NOTIF_MASTER_ENABLED = "NOTIF_MASTER_ENABLED";
    public static final String NOTIF_EMAIL_CREATION = "NOTIF_EMAIL_CREATION";
    public static final String NOTIF_EMAIL_ASSIGNMENT = "NOTIF_EMAIL_ASSIGNMENT";
    public static final String NOTIF_EMAIL_PROGRESS = "NOTIF_EMAIL_PROGRESS";
    public static final String NOTIF_EMAIL_RESOLUTION = "NOTIF_EMAIL_RESOLUTION";
    public static final String NOTIF_EMAIL_AUTO_CLOSE = "NOTIF_EMAIL_AUTO_CLOSE";
    public static final String NOTIF_EMAIL_CC_SUPERVISORS = "NOTIF_EMAIL_CC_SUPERVISORS";
    
    /**
     * Obtener todas las configuraciones de notificaciones
     */
    public Map<String, Boolean> obtenerConfiguracionesNotificaciones() {
        Map<String, Boolean> config = new HashMap<>();
        
        // Obtener todas las configuraciones con valores predeterminados
        config.put(NOTIF_MASTER_ENABLED, configService.obtenerValorBooleano(NOTIF_MASTER_ENABLED, false));
        config.put(NOTIF_EMAIL_CREATION, configService.obtenerValorBooleano(NOTIF_EMAIL_CREATION, false));
        config.put(NOTIF_EMAIL_ASSIGNMENT, configService.obtenerValorBooleano(NOTIF_EMAIL_ASSIGNMENT, false));
        config.put(NOTIF_EMAIL_PROGRESS, configService.obtenerValorBooleano(NOTIF_EMAIL_PROGRESS, false));
        config.put(NOTIF_EMAIL_RESOLUTION, configService.obtenerValorBooleano(NOTIF_EMAIL_RESOLUTION, false));
        config.put(NOTIF_EMAIL_AUTO_CLOSE, configService.obtenerValorBooleano(NOTIF_EMAIL_AUTO_CLOSE, false));
        config.put(NOTIF_EMAIL_CC_SUPERVISORS, configService.obtenerValorBooleano(NOTIF_EMAIL_CC_SUPERVISORS, false));
        
        return config;
    }
    
    /**
     * Guardar todas las configuraciones de notificaciones
     */
    @Transactional
    public boolean guardarConfiguracionesNotificaciones(Map<String, Boolean> configuraciones) {
        try {
            for (Map.Entry<String, Boolean> entry : configuraciones.entrySet()) {
                ConfiguracionSistema config = configService.obtenerPorClave(entry.getKey())
                    .orElse(new ConfiguracionSistema(entry.getKey(), 
                                                    String.valueOf(entry.getValue()), 
                                                    "Configuración de notificaciones"));
                config.setValor(String.valueOf(entry.getValue()));
                configService.guardar(config);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verificar si un tipo de notificación está habilitado
     * (Respeta la jerarquía - si el principal está desactivado, todos están desactivados)
     */
    public boolean isNotificacionHabilitada(String tipoNotificacion) {
        // Si el switch principal está desactivado, ninguna notificación está habilitada
        if (!configService.obtenerValorBooleano(NOTIF_MASTER_ENABLED, false)) {
            return false;
        }
        
        // Si el switch principal está activado, verificamos el tipo específico
        return configService.obtenerValorBooleano(tipoNotificacion, false);
    }
}