package com.tickets.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tickets.backend.models.ConfiguracionCorreo;
import com.tickets.backend.repository.ConfiguracionCorreoRepository;

@Service
public class ConfiguracionCorreoService {

    @Autowired
    private ConfiguracionCorreoRepository configuracionCorreoRepository;
    
    /**
     * Obtiene todas las configuraciones de correo
     * @return Lista de configuraciones
     */
    public List<ConfiguracionCorreo> obtenerTodas() {
        return configuracionCorreoRepository.findAll();
    }
    
    /**
     * Obtiene una configuración por ID
     * @param id ID de la configuración
     * @return Configuración si existe
     */
    public Optional<ConfiguracionCorreo> obtenerPorId(Long id) {
        return configuracionCorreoRepository.findById(id);
    }
    
    /**
     * Obtiene la configuración activa
     * @return Configuración activa si existe
     */
    public Optional<ConfiguracionCorreo> obtenerConfiguracionActiva() {
        return configuracionCorreoRepository.findConfiguracionActiva();
    }
    
    /**
     * Guarda una configuración
     * @param configuracion Configuración a guardar
     * @return Configuración guardada
     */
    @Transactional
    public ConfiguracionCorreo guardar(ConfiguracionCorreo configuracion) {
        // Si la nueva configuración es activa, desactivar todas las demás
        if (Boolean.TRUE.equals(configuracion.getActivo())) {
            desactivarTodasLasConfiguraciones();
        }
        return configuracionCorreoRepository.save(configuracion);
    }
    
    /**
     * Activa una configuración y desactiva las demás
     * @param id ID de la configuración a activar
     * @return true si se activó correctamente
     */
    @Transactional
    public boolean activarConfiguracion(Long id) {
        Optional<ConfiguracionCorreo> configuracionOpt = configuracionCorreoRepository.findById(id);
        
        if (configuracionOpt.isPresent()) {
            // Desactivar todas las configuraciones
            desactivarTodasLasConfiguraciones();
            
            // Activar la configuración seleccionada
            ConfiguracionCorreo configuracion = configuracionOpt.get();
            configuracion.setActivo(true);
            configuracionCorreoRepository.save(configuracion);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Desactiva todas las configuraciones de correo
     */
    @Transactional
    public void desactivarTodasLasConfiguraciones() {
        List<ConfiguracionCorreo> configuraciones = configuracionCorreoRepository.findAll();
        for (ConfiguracionCorreo config : configuraciones) {
            config.setActivo(false);
            configuracionCorreoRepository.save(config);
        }
    }
    
    /**
     * Elimina una configuración
     * @param id ID de la configuración
     */
    @Transactional
    public void eliminar(Long id) {
        configuracionCorreoRepository.deleteById(id);
    }
}