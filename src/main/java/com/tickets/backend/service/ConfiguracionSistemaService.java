package com.tickets.backend.service;

import com.tickets.backend.models.ConfiguracionSistema;
import com.tickets.backend.repository.ConfiguracionSistemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConfiguracionSistemaService {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionSistemaService.class);

    @Autowired
    private ConfiguracionSistemaRepository configuracionRepository;
    
    /**
     * Obtener todas las configuraciones
     */
    public List<ConfiguracionSistema> obtenerTodas() {
        return configuracionRepository.findAll();
    }
    
    /**
     * Obtener configuración por ID
     */
    public Optional<ConfiguracionSistema> obtenerPorId(Long id) {
        return configuracionRepository.findById(id);
    }
    
    /**
     * Obtener configuración por clave
     */
    public Optional<ConfiguracionSistema> obtenerPorClave(String clave) {
        logger.info("Buscando configuración por clave: {}", clave);
        
        Optional<ConfiguracionSistema> configuracion = configuracionRepository.findByClave(clave);
        
        if (configuracion.isPresent()) {
            logger.info("Configuración encontrada - Clave: {}, Valor: {}", 
                    clave, configuracion.get().getValor());
        } else {
            logger.warn("No se encontró configuración para la clave: {}", clave);
        }
        
        return configuracion;
    }
    
    @Transactional
    public void eliminar(Long id) {
        configuracionRepository.deleteById(id);
    }
    
    /**
     * Eliminar una configuración por clave
     */
    @Transactional
    public void eliminarPorClave(String clave) {
        configuracionRepository.deleteByClave(clave);
    }
    
    /**
     * Obtener valor de una configuración con valor por defecto si no existe
     */
    public String obtenerValor(String clave, String valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(ConfiguracionSistema::getValor)
                .orElse(valorPorDefecto);
    }
    
    /**
     * Obtener valor numérico de una configuración con valor por defecto
     */
    public int obtenerValorNumerico(String clave, int valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getValor());
                    } catch (NumberFormatException e) {
                        return valorPorDefecto;
                    }
                })
                .orElse(valorPorDefecto);
    }
    
    /**
     * Obtener valor booleano de una configuración con valor por defecto
     */
    public boolean obtenerValorBooleano(String clave, boolean valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(config -> Boolean.parseBoolean(config.getValor()))
                .orElse(valorPorDefecto);
    }
    
    /**
     * Obtener configuraciones por tipo (agrupación funcional)
     * Las claves deben seguir un formato como: TIPO_SUBTIPO_NOMBRE
     */
    public List<ConfiguracionSistema> obtenerPorTipo(String tipo) {
        // Simplificación: buscar todas las configuraciones cuya clave comience con el tipo
        return configuracionRepository.findAll().stream()
                .filter(config -> config.getClave().startsWith(tipo.toUpperCase() + "_") || 
                                  config.getClave().contains("_" + tipo.toUpperCase() + "_"))
                .collect(Collectors.toList());
    }

    /**
     * Obtener configuraciones por claves específicas - MÉTODO CORREGIDO
     */
    public List<ConfiguracionSistema> obtenerPorClaves(List<String> claves) {
        List<ConfiguracionSistema> configuraciones = new ArrayList<>();
        
        // Primero buscamos las existentes
        List<ConfiguracionSistema> existentes = configuracionRepository.findByClaveIn(claves);
        configuraciones.addAll(existentes);
        
        // Obtenemos las claves que ya encontramos
        List<String> clavesEncontradas = existentes.stream()
                .map(ConfiguracionSistema::getClave)
                .collect(Collectors.toList());
        
        // Para las no encontradas, creamos configuraciones por defecto
        for (String clave : claves) {
            if (!clavesEncontradas.contains(clave)) {
                ConfiguracionSistema nuevaConfig = new ConfiguracionSistema();
                nuevaConfig.setClave(clave);
                
                // Valores predeterminados según la clave - ACTUALIZADO CON NUEVAS CONFIGURACIONES
                switch (clave) {
                    case "DIAS_PARA_CIERRE_AUTOMATICO":
                        nuevaConfig.setValor("1");
                        nuevaConfig.setDescripcion("Días de espera para cerrar automáticamente tickets resueltos");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    case "CIERRE_AUTOMATICO_HABILITADO":
                        nuevaConfig.setValor("true");
                        nuevaConfig.setDescripcion("Habilitar/deshabilitar cierre automático de tickets");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    case "PREFIJO_NUMERO_TICKET":
                        nuevaConfig.setValor("TK-");
                        nuevaConfig.setDescripcion("Prefijo utilizado para la numeración de tickets");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    case "DIGITOS_NUMERO_TICKET":
                        nuevaConfig.setValor("6");
                        nuevaConfig.setDescripcion("Cantidad de dígitos en el número correlativo de tickets");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    // NUEVAS CONFIGURACIONES PARA HORARIO
                    case "FRECUENCIA_CIERRE_AUTOMATICO":
                        nuevaConfig.setValor("DIARIO");
                        nuevaConfig.setDescripcion("Frecuencia de ejecución del cierre automático");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    case "HORARIO_CIERRE_AUTOMATICO":
                        nuevaConfig.setValor("9");
                        nuevaConfig.setDescripcion("Hora del día para ejecutar cierre automático (0-23)");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    case "MINUTOS_CIERRE_AUTOMATICO":
                        nuevaConfig.setValor("0");
                        nuevaConfig.setDescripcion("Minutos de la hora para ejecutar cierre automático (0-59)");
                        nuevaConfig.setTipo("TICKETS");
                        break;
                    default:
                        nuevaConfig.setValor("");
                        nuevaConfig.setDescripcion("Configuración automática");
                        nuevaConfig.setTipo("SISTEMA");
                }
                
                configuraciones.add(nuevaConfig);
            }
        }
        
        return configuraciones;
    }

    /**
     * NUEVO MÉTODO: Obtiene las configuraciones específicas para tickets incluyendo horario
     */
    public List<ConfiguracionSistema> obtenerConfiguracionCompleta() {
        List<String> claves = List.of(
            "DIAS_PARA_CIERRE_AUTOMATICO", 
            "CIERRE_AUTOMATICO_HABILITADO",
            "PREFIJO_NUMERO_TICKET",
            "DIGITOS_NUMERO_TICKET",
            "FRECUENCIA_CIERRE_AUTOMATICO",
            "HORARIO_CIERRE_AUTOMATICO",
            "MINUTOS_CIERRE_AUTOMATICO"
        );
        
        return obtenerPorClaves(claves);
    }

    /**
     * Guardar una configuración
     */
    public ConfiguracionSistema guardar(ConfiguracionSistema configuracion) {
        return configuracionRepository.save(configuracion);
    }

    /**
     * Guardar varias configuraciones
     */
    @Transactional
    public List<ConfiguracionSistema> guardarTodas(List<ConfiguracionSistema> configuraciones) {
        List<ConfiguracionSistema> resultado = new ArrayList<>();
        
        for (ConfiguracionSistema config : configuraciones) {
            // Buscar si la configuración ya existe
            Optional<ConfiguracionSistema> existente = configuracionRepository.findByClave(config.getClave());
            
            if (existente.isPresent()) {
                // Actualizar la existente
                ConfiguracionSistema configExistente = existente.get();
                configExistente.setValor(config.getValor());
                
                // Actualizar la descripción si se proporcionó una nueva
                if (config.getDescripcion() != null && !config.getDescripcion().isEmpty()) {
                    configExistente.setDescripcion(config.getDescripcion());
                }
                
                // Actualizar el tipo si se proporcionó uno nuevo
                if (config.getTipo() != null && !config.getTipo().isEmpty()) {
                    configExistente.setTipo(config.getTipo());
                }
                
                resultado.add(configuracionRepository.save(configExistente));
            } else {
                // Crear una nueva
                resultado.add(configuracionRepository.save(config));
            }
        }
        
        return resultado;
    }

    /**
     * Actualizar una configuración específica
     */
    public ConfiguracionSistema actualizarConfiguracion(String clave, String valor) {
        logger.info("Actualizando configuración - Clave: {}, Nuevo valor: {}", clave, valor);
        
        Optional<ConfiguracionSistema> configuracionOpt = obtenerPorClave(clave);
        ConfiguracionSistema configuracion;
        
        if (configuracionOpt.isPresent()) {
            configuracion = configuracionOpt.get();
            String valorAnterior = configuracion.getValor();
            configuracion.setValor(valor);
            logger.info("Actualizando configuración existente - Clave: {}, Valor anterior: {}, Nuevo valor: {}", 
                    clave, valorAnterior, valor);
        } else {
            logger.info("Creando nueva configuración - Clave: {}, Valor: {}", clave, valor);
            configuracion = new ConfiguracionSistema();
            configuracion.setClave(clave);
            configuracion.setValor(valor);
            configuracion.setDescripcion("Configuración generada automáticamente");
            configuracion.setTipo("SISTEMA");
        }
        
        configuracion = configuracionRepository.save(configuracion);
        logger.info("Configuración guardada con éxito - ID: {}", configuracion.getId());
        
        return configuracion;
    }
}