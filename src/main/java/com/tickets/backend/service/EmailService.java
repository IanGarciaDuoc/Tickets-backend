package com.tickets.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.tickets.backend.config.MailConfig;
import com.tickets.backend.models.ConfiguracionCorreo;
import com.tickets.backend.models.PrioridadTicket;
import com.tickets.backend.models.Ticket;
import com.tickets.backend.repository.ConfiguracionCorreoRepository;
import com.tickets.backend.repository.ConfiguracionSistemaRepository;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private ConfiguracionSistemaRepository configuracionRepository;
    
    @Autowired
    private ConfiguracionCorreoRepository configuracionCorreoRepository;
    
    @Autowired
    private MailConfig mailConfig;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String defaultFromEmail;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    /**
     * Actualiza el JavaMailSender cuando cambia la configuración
     */
    public void actualizarMailSender() {
        Optional<ConfiguracionCorreo> configuracionOpt = configuracionCorreoRepository.findConfiguracionActiva();
        if (configuracionOpt.isPresent()) {
            this.emailSender = mailConfig.reconstruirMailSender(configuracionOpt.get());
            logger.info("JavaMailSender actualizado con la nueva configuración");
        } else {
            logger.warn("No se pudo actualizar el JavaMailSender. No hay configuración activa.");
        }
    }
    
    /**
     * Prueba la configuración de correo especificada
     */
    public boolean probarConfiguracion(ConfiguracionCorreo config, String emailDestino) {
        try {
            // Crear un JavaMailSender temporal con la configuración a probar
            JavaMailSender tempMailSender = mailConfig.reconstruirMailSender(config);
            
            MimeMessage message = tempMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(config.getRemitente());
            helper.setTo(emailDestino);
            helper.setSubject("Prueba de configuración de correo");
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("titulo", "Prueba de Configuración");
            context.setVariable("mensaje", "Esta es una prueba de configuración de correo electrónico.");
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/prueba-configuracion", context);
            
            helper.setText(content, true);
            
            tempMailSender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("Error al probar configuración de correo: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el correo remitente desde la configuración activa en base de datos
     * Si no existe, busca en la configuración del sistema
     * Si tampoco existe, usa el valor por defecto de application.properties
     */
    private String getFromEmail() {
        try {
            // Primero intentamos obtener de la configuración de correo activa
            Optional<ConfiguracionCorreo> configuracionCorreoOpt = configuracionCorreoRepository.findConfiguracionActiva();
            if (configuracionCorreoOpt.isPresent()) {
                return configuracionCorreoOpt.get().getRemitente();
            }
            
            // Si no hay configuración de correo activa, buscamos en configuraciones generales
            return configuracionRepository.findByClave("EMAIL_REMITENTE")
                .map(config -> config.getValor())
                .orElse(defaultFromEmail);
        } catch (Exception e) {
            logger.error("Error al obtener el correo remitente de la configuración: {}", e.getMessage());
            return defaultFromEmail;
        }
    }
    
    /**
     * Verifica si las notificaciones están habilitadas
     */
    private boolean areNotificacionesHabilitadas() {
        try {
            return configuracionRepository.findByClave("NOTIF_MASTER_ENABLED")
                .map(config -> Boolean.parseBoolean(config.getValor()))
                .orElse(true); // Por defecto habilitadas
        } catch (Exception e) {
            logger.error("Error al verificar si las notificaciones están habilitadas: {}", e.getMessage());
            return true; // Por defecto habilitadas
        }
    }
    
    /**
     * Verifica si un tipo específico de notificación está habilitado
     */
    private boolean isNotificacionTipoHabilitada(String tipoNotificacion) {
        // Si las notificaciones generales están deshabilitadas, retornar false
        if (!areNotificacionesHabilitadas()) {
            return false;
        }
        
        try {
            return configuracionRepository.findByClave(tipoNotificacion)
                .map(config -> Boolean.parseBoolean(config.getValor()))
                .orElse(true); // Por defecto habilitadas
        } catch (Exception e) {
            logger.error("Error al verificar si la notificación {} está habilitada: {}", tipoNotificacion, e.getMessage());
            return true; // Por defecto habilitadas
        }
    }
    
    /**
     * Verifica si se debe incluir a los supervisores en copia
     */
    private boolean isIncluirSupervisores() {
        try {
            return configuracionRepository.findByClave("NOTIF_EMAIL_CC_SUPERVISORS")
                .map(config -> Boolean.parseBoolean(config.getValor()))
                .orElse(false); // Por defecto no
        } catch (Exception e) {
            logger.error("Error al verificar si se incluyen supervisores en copia: {}", e.getMessage());
            return false; // Por defecto no
        }
    }

    public void enviarCorreoRecuperacionPassword(String toEmail, String token) {
        // Verificar si las notificaciones están habilitadas
        if (!areNotificacionesHabilitadas()) {
            logger.info("Notificaciones deshabilitadas. No se enviará correo de recuperación.");
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de Contraseña");
            
            // La URL del frontend se debe construir dinámicamente
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/recuperacion-contrasena", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de recuperación de contraseña enviado a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de recuperación: {}", e.getMessage());
            throw new RuntimeException("Error al enviar correo de recuperación", e);
        }
    }

    public void enviarNotificacionTicketCreado(Ticket ticket) {
        // Verificar si las notificaciones de creación están habilitadas
        if (!isNotificacionTipoHabilitada("NOTIF_EMAIL_CREATION")) {
            logger.info("Notificaciones de creación deshabilitadas. No se enviará correo para ticket #{}.", ticket.getNumeroTicket());
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            helper.setTo(ticket.getUsuarioCreador().getEmail());
            helper.setSubject("Ticket #" + ticket.getNumeroTicket() + " - " + ticket.getTitulo());
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("ticket", ticket);
            context.setVariable("urlBase", frontendUrl);
            context.setVariable("prioridadColor", getPrioridadColor(ticket.getPrioridad()));
            context.setVariable("prioridadTextColor", getPrioridadTextColor(ticket.getPrioridad()));
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/ticket-creado", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de notificación de ticket creado enviado a: {}", ticket.getUsuarioCreador().getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de notificación de ticket creado: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }
    
    /**
     * Envía correos de notificación a los destinatarios configurados para la categoría
     */
    public void enviarNotificacionCategoria(Ticket ticket, List<String> destinatarios) {
        // Verificar si las notificaciones de creación están habilitadas
        if (!isNotificacionTipoHabilitada("NOTIF_EMAIL_CREATION")) {
            logger.info("Notificaciones de creación deshabilitadas. No se enviará correo para categoría {}.", ticket.getCategoria().getNombre());
            return;
        }
        
        if (destinatarios == null || destinatarios.isEmpty()) {
            logger.warn("No hay destinatarios configurados para la categoría {}", ticket.getCategoria().getNombre());
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            helper.setSubject("Nuevo Ticket #" + ticket.getNumeroTicket() + " - Categoría: " + ticket.getCategoria().getNombre());
            
            // Convertir lista a array para el método setTo
            String[] destinatariosArray = destinatarios.toArray(new String[0]);
            helper.setTo(destinatariosArray);
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("ticket", ticket);
            context.setVariable("urlBase", frontendUrl);
            context.setVariable("prioridadColor", getPrioridadColor(ticket.getPrioridad()));
            context.setVariable("prioridadTextColor", getPrioridadTextColor(ticket.getPrioridad()));
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/notificacion-categoria", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de notificación de categoría enviado a: {}", String.join(", ", destinatarios));
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de notificación a categoría: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }
    
    public void enviarNotificacionTicketAsignado(Ticket ticket) {
        // Verificar si las notificaciones de asignación están habilitadas
        if (!isNotificacionTipoHabilitada("NOTIF_EMAIL_ASSIGNMENT")) {
            logger.info("Notificaciones de asignación deshabilitadas. No se enviará correo para ticket #{}.", ticket.getNumeroTicket());
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            
            // Enviamos al creador del ticket
            helper.setTo(ticket.getUsuarioCreador().getEmail());
            
            // Copia al técnico asignado
            if (ticket.getTecnicoAsignado() != null) {
                helper.setCc(ticket.getTecnicoAsignado().getEmail());
            }
            
            helper.setSubject("Ticket #" + ticket.getNumeroTicket() + " - Asignado");
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("ticket", ticket);
            context.setVariable("urlBase", frontendUrl);
            context.setVariable("prioridadColor", getPrioridadColor(ticket.getPrioridad()));
            context.setVariable("prioridadTextColor", getPrioridadTextColor(ticket.getPrioridad()));
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/ticket-asignado", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de notificación de ticket asignado enviado a: {}", ticket.getUsuarioCreador().getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de notificación de ticket asignado: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }
    
    public void enviarNotificacionTicketEnProgreso(Ticket ticket) {
        // Verificar si las notificaciones de progreso están habilitadas
        if (!isNotificacionTipoHabilitada("NOTIF_EMAIL_PROGRESS")) {
            logger.info("Notificaciones de progreso deshabilitadas. No se enviará correo para ticket #{}.", ticket.getNumeroTicket());
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            
            // Enviamos al creador del ticket
            helper.setTo(ticket.getUsuarioCreador().getEmail());
            
            // Copia al técnico asignado
            if (ticket.getTecnicoAsignado() != null) {
                helper.setCc(ticket.getTecnicoAsignado().getEmail());
            }
            
            helper.setSubject("Ticket #" + ticket.getNumeroTicket() + " - En curso");
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("ticket", ticket);
            context.setVariable("urlBase", frontendUrl);
            context.setVariable("prioridadColor", getPrioridadColor(ticket.getPrioridad()));
            context.setVariable("prioridadTextColor", getPrioridadTextColor(ticket.getPrioridad()));
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/ticket-en-progreso", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de notificación de ticket en curso enviado a: {}", ticket.getUsuarioCreador().getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de notificación de ticket en curso: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }
    
    /**
     * Envía notificación cuando un ticket es resuelto
     */
    public void enviarNotificacionTicketResuelto(Ticket ticket) {
        // Verificar si las notificaciones de resolución están habilitadas
        if (!isNotificacionTipoHabilitada("NOTIF_EMAIL_RESOLUTION")) {
            logger.info("Notificaciones de resolución deshabilitadas. No se enviará correo para ticket #{}.", ticket.getNumeroTicket());
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            
            // Enviamos al creador del ticket
            helper.setTo(ticket.getUsuarioCreador().getEmail());
            
            // Copia al técnico asignado
            if (ticket.getTecnicoAsignado() != null) {
                helper.setCc(ticket.getTecnicoAsignado().getEmail());
            }
            
            helper.setSubject("Ticket #" + ticket.getNumeroTicket() + " - Resuelto");
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("ticket", ticket);
            context.setVariable("urlBase", frontendUrl);
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/ticket-resuelto", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de notificación de ticket resuelto enviado a: {}", ticket.getUsuarioCreador().getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de notificación de ticket resuelto: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }
    
    /**
     * Envía notificación cuando un ticket se cierra automáticamente
     */
    public void enviarNotificacionCierreAutomatico(Ticket ticket) {
        // Verificar si las notificaciones de cierre automático están habilitadas
        if (!isNotificacionTipoHabilitada("NOTIF_EMAIL_AUTO_CLOSE")) {
            logger.info("Notificaciones de cierre automático deshabilitadas. No se enviará correo para ticket #{}.", ticket.getNumeroTicket());
            return;
        }
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Usar el correo remitente configurado
            helper.setFrom(getFromEmail());
            
            // Enviamos al creador del ticket
            helper.setTo(ticket.getUsuarioCreador().getEmail());
            
            // Copia al técnico asignado
            if (ticket.getTecnicoAsignado() != null) {
                helper.setCc(ticket.getTecnicoAsignado().getEmail());
            }
            
            helper.setSubject("Ticket #" + ticket.getNumeroTicket() + " - Cerrado automáticamente");
            
            // Crear contexto para la plantilla
            Context context = new Context();
            context.setVariable("ticket", ticket);
            context.setVariable("urlBase", frontendUrl);
            
            // Procesar la plantilla
            String content = templateEngine.process("emails/ticket-cerrado-automatico", context);
            
            helper.setText(content, true);
            
            emailSender.send(message);
            logger.info("Correo de notificación de cierre automático enviado a: {}", ticket.getUsuarioCreador().getEmail());
        } catch (MessagingException e) {
            logger.error("Error al enviar correo de notificación de cierre automático: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
        }
    }
    
    private String getPrioridadColor(PrioridadTicket prioridad) {
        if (prioridad == null) {
            return "#9e9e9e"; // Gris para nulo
        }
        
        switch (prioridad) {
            case BAJA:
                return "#16A34A"; // Verde como en el frontend
            case MEDIA:
                return "#FCD34D"; // Amarillo como en el frontend
            case ALTA:
                return "#ffa502"; // Naranja como en el frontend
            case CRITICA:
                return "#EF4444"; // Rojo como en el frontend
            default:
                return "#9e9e9e"; // Gris para otros casos
        }
    }
    
    private String getPrioridadTextColor(PrioridadTicket prioridad) {
        if (prioridad == null) {
            return "white";
        }
        
        switch (prioridad) {
            case MEDIA:
                return "#92400E"; // Texto marrón para prioridad media (fondo amarillo)
            default:
                return "white"; // Blanco para las demás
        }
    }
    
    // Color para los estados que coincide con el frontend
    private String getEstadoColor(String estado) {
        if (estado == null) {
            return "#9e9e9e"; // Gris para nulo
        }
        
        String estadoUpper = estado.toUpperCase();
        
        switch (estadoUpper) {
            case "NUEVO":
                return "#00a8ff"; // Azul brillante
            case "ASIGNADO":
                return "#8c7ae6"; // Violeta
            case "EN_PROGRESO":
            case "EN PROGRESO":
            case "EN_CURSO":
            case "EN CURSO":
                return "#00b894"; // Verde-azulado
            case "PENDIENTE":
                return "#64748B"; // Gris azulado
            case "EN_ESPERA":
            case "EN ESPERA":
                return "#9CA3AF"; // Gris
            case "RESUELTO":
                return "#10B981"; // Verde
            case "CERRADO":
                return "#374151"; // Gris oscuro
            default:
                return "#9e9e9e"; // Gris para otros casos
        }
    }
}