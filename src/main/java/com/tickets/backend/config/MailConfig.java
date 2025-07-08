package com.tickets.backend.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.tickets.backend.models.ConfiguracionCorreo;
import com.tickets.backend.repository.ConfiguracionCorreoRepository;

@Configuration
public class MailConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);
    
    @Autowired
    private ConfiguracionCorreoRepository configuracionCorreoRepository;
    
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // Intentar cargar configuración desde base de datos
        try {
            configuracionCorreoRepository.findConfiguracionActiva()
                .ifPresent(config -> configurarMailSender(mailSender, config));
        } catch (Exception e) {
            logger.error("Error al cargar configuración de correo desde BD: {}", e.getMessage());
            logger.warn("Usando configuración por defecto del application.properties");
            // Si hay error, se usará la configuración de application.properties
        }
        
        return mailSender;
    }
    
    /**
     * Configura el JavaMailSender con la configuración de BD
     */
    private void configurarMailSender(JavaMailSenderImpl mailSender, ConfiguracionCorreo config) {
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPuerto());
        mailSender.setUsername(config.getUsuario());
        mailSender.setPassword(config.getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", config.getAuthRequerida().toString());
        props.put("mail.smtp.starttls.enable", config.getTlsHabilitado().toString());
        props.put("mail.smtp.ssl.enable", config.getSslHabilitado().toString());
        props.put("mail.debug", "true"); // Útil para depuración
        
        
    }
    
    /**
     * Método para reconstruir el JavaMailSender con nueva configuración
     * Este método será usado cuando se actualice la configuración
     */
    public JavaMailSender reconstruirMailSender(ConfiguracionCorreo config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        configurarMailSender(mailSender, config);
        return mailSender;
    }
}