package com.tickets.backend.service;

import com.tickets.backend.models.EstadoTicket;
import com.tickets.backend.models.HistorialCambio;
import com.tickets.backend.models.Ticket;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.HistorialCambioRepository;
import com.tickets.backend.repository.TicketRepository;
import com.tickets.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
public class TicketCierreAutomaticoService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketCierreAutomaticoService.class);
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private ConfiguracionSistemaService configuracionService;
    
    @Autowired
    private HistorialCambioRepository historialCambioRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    private ScheduledFuture<?> tareaActual;
    
    /**
     * Inicializa el scheduler con la configuración actual
     */
    @PostConstruct
    public void inicializarScheduler() {
        logger.info("Inicializando scheduler de cierre automático");
        actualizarScheduler();
    }
    
    /**
     * Actualiza el scheduler con la nueva configuración
     */
    public void actualizarScheduler() {
        logger.info("Actualizando scheduler de cierre automático");
        
        // Cancelar la tarea actual si existe
        if (tareaActual != null && !tareaActual.isCancelled()) {
            tareaActual.cancel(false);
            logger.info("Tarea anterior cancelada");
        }
        
        // Verificar si el cierre automático está habilitado
        boolean habilitado = configuracionService.obtenerValorBooleano(
            "CIERRE_AUTOMATICO_HABILITADO", true
        );
        
        if (!habilitado) {
            logger.info("Cierre automático deshabilitado");
            return;
        }
        
        // Obtener configuración de horario
        String expresionCron = generarExpresionCron();
        
        try {
            // Programar la nueva tarea
            tareaActual = taskScheduler.schedule(
                this::procesarCierreAutomaticoTickets,
                new CronTrigger(expresionCron)
            );
            
            logger.info("Scheduler actualizado con expresión cron: {}", expresionCron);
            
        } catch (Exception e) {
            logger.error("Error al actualizar scheduler: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Genera la expresión cron basada en la configuración
     */
    private String generarExpresionCron() {
        String frecuencia = configuracionService.obtenerValor("FRECUENCIA_CIERRE_AUTOMATICO", "DIARIO");
        int hora = configuracionService.obtenerValorNumerico("HORARIO_CIERRE_AUTOMATICO", 9);
        int minutos = configuracionService.obtenerValorNumerico("MINUTOS_CIERRE_AUTOMATICO", 0);
        
        // Validar rangos
        hora = Math.max(0, Math.min(23, hora));
        minutos = Math.max(0, Math.min(59, minutos));
        
        String expresion;
        switch (frecuencia.toUpperCase()) {
            case "CADA_HORA":
                expresion = String.format("0 %d * * * *", minutos);
                break;
            case "CADA_6_HORAS":
                expresion = String.format("0 %d */6 * * *", minutos);
                break;
            case "DIARIO":
            default:
                expresion = String.format("0 %d %d * * *", minutos, hora);
                break;
        }
        
        logger.info("Expresión cron generada: {} para frecuencia: {}, hora: {}:{}", 
            expresion, frecuencia, hora, minutos);
        
        return expresion;
    }
    
    /**
     * Proceso principal de cierre automático
     */
    @Transactional
    public void procesarCierreAutomaticoTickets() {
        logger.info("Iniciando proceso de cierre automático de tickets");
        
        try {
            // Verificar si el cierre automático está habilitado
            boolean cierreAutomaticoHabilitado = configuracionService.obtenerValorBooleano(
                "CIERRE_AUTOMATICO_HABILITADO", true
            );
            
            if (!cierreAutomaticoHabilitado) {
                logger.info("Cierre automático deshabilitado por configuración");
                return;
            }
            
            // Obtener días de espera desde configuración
            int diasParaCierre = configuracionService.obtenerValorNumerico(
                "DIAS_PARA_CIERRE_AUTOMATICO", 1
            );
            
            logger.info("Buscando tickets resueltos hace {} días o más", diasParaCierre);
            
            // Calcular fecha límite
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasParaCierre);
            
            // Buscar tickets resueltos que cumplan el criterio
            List<Ticket> ticketsParaCerrar = ticketRepository.findByEstadoAndFechaResolucionLessThan(
                EstadoTicket.RESUELTO, fechaLimite
            );
            
            logger.info("Encontrados {} tickets para cerrar automáticamente", ticketsParaCerrar.size());
            
            // Procesar cada ticket
            int ticketsCerrados = 0;
            int errores = 0;
            
            for (Ticket ticket : ticketsParaCerrar) {
                try {
                    cerrarTicketAutomaticamente(ticket, diasParaCierre);
                    ticketsCerrados++;
                    logger.debug("Ticket {} cerrado automáticamente", ticket.getNumeroTicket());
                } catch (Exception e) {
                    errores++;
                    logger.error("Error al cerrar automáticamente el ticket {}: {}", 
                        ticket.getNumeroTicket(), e.getMessage(), e);
                }
            }
            
            logger.info("Proceso completado. Tickets cerrados: {}, Errores: {}", ticketsCerrados, errores);
            
            // Actualizar estadísticas
            actualizarEstadisticasCierreAutomatico(ticketsCerrados);
            
        } catch (Exception e) {
            logger.error("Error en el proceso de cierre automático de tickets", e);
        }
    }
    
    /**
     * Cierra un ticket específico automáticamente
     */
   private void cerrarTicketAutomaticamente(Ticket ticket, int diasEspera) {
    EstadoTicket estadoAnterior = ticket.getEstado();
    
    // Cambiar estado a CERRADO
    ticket.setEstado(EstadoTicket.CERRADO);
    ticket.setFechaActualizacion(LocalDateTime.now());
    ticket.setFechaCierre(LocalDateTime.now());
    
    // Guardar el ticket
    ticketRepository.save(ticket);
    
    // Registrar el cambio en el historial
    registrarCambioAutomatico(ticket, estadoAnterior, EstadoTicket.CERRADO, diasEspera);
    
    if (diasEspera == 0) {
        logger.info("Ticket {} cerrado manualmente", ticket.getNumeroTicket());
    } else {
        logger.info("Ticket {} cerrado automáticamente después de {} días", 
            ticket.getNumeroTicket(), diasEspera);
    }
}
    
    /**
     * Registra el cambio automático en el historial
     */
    private void registrarCambioAutomatico(Ticket ticket, EstadoTicket estadoAnterior, 
                                         EstadoTicket estadoNuevo, int diasEspera) {
        try {
            Usuario usuarioSistema = obtenerUsuarioSistema();
            
            HistorialCambio cambio = new HistorialCambio();
            cambio.setTicket(ticket);
            cambio.setUsuario(usuarioSistema);
            cambio.setCampoModificado("estado");
            cambio.setValorAnterior(estadoAnterior.toString());
            cambio.setValorNuevo(estadoNuevo.toString());
            cambio.setFechaCambio(LocalDateTime.now());
            
            historialCambioRepository.save(cambio);
            
        } catch (Exception e) {
            logger.error("Error al registrar el cambio automático para el ticket {}: {}", 
                ticket.getNumeroTicket(), e.getMessage());
        }
    }
    
    /**
     * Obtiene un usuario del sistema para registrar cambios automáticos
     */
    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findByEmail("sistema@helpdesk.com")
            .orElseGet(() -> {
                return usuarioRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getNombre().equals("ROLE_ADMIN")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                        "No se encontró un usuario del sistema para registrar cambios automáticos"));
            });
    }
    
    /**
     * Actualiza las estadísticas de cierre automático
     */
    private void actualizarEstadisticasCierreAutomatico(int ticketsCerrados) {
        try {
            int totalCierresAutomaticos = configuracionService.obtenerValorNumerico(
                "TOTAL_CIERRES_AUTOMATICOS", 0
            );
            
            totalCierresAutomaticos += ticketsCerrados;
            
            configuracionService.actualizarConfiguracion(
                "TOTAL_CIERRES_AUTOMATICOS", 
                String.valueOf(totalCierresAutomaticos)
            );
            
            configuracionService.actualizarConfiguracion(
                "ULTIMA_EJECUCION_CIERRE_AUTOMATICO", 
                LocalDateTime.now().toString()
            );
            
            logger.info("Estadísticas actualizadas: Total cierres automáticos = {}", totalCierresAutomaticos);
            
        } catch (Exception e) {
            logger.error("Error al actualizar estadísticas de cierre automático", e);
        }
    }
    
    /**
     * Método manual para ejecutar el cierre automático
     */
    @Transactional
    public void ejecutarCierreAutomaticoManual() {
        logger.info("Ejecutando cierre automático manualmente");
        procesarCierreAutomaticoTickets();
    }
    
    /**
     * Obtiene información sobre el próximo cierre automático
     */
    public String obtenerInformacionProximoCierre() {
        try {
            boolean habilitado = configuracionService.obtenerValorBooleano(
                "CIERRE_AUTOMATICO_HABILITADO", true
            );
            
            if (!habilitado) {
                return "El cierre automático está deshabilitado";
            }
            
            int diasParaCierre = configuracionService.obtenerValorNumerico(
                "DIAS_PARA_CIERRE_AUTOMATICO", 1
            );
            
            String frecuencia = configuracionService.obtenerValor("FRECUENCIA_CIERRE_AUTOMATICO", "DIARIO");
            int hora = configuracionService.obtenerValorNumerico("HORARIO_CIERRE_AUTOMATICO", 9);
            int minutos = configuracionService.obtenerValorNumerico("MINUTOS_CIERRE_AUTOMATICO", 0);
            
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasParaCierre);
            List<Ticket> ticketsParaCerrar = ticketRepository.findByEstadoAndFechaResolucionLessThan(
                EstadoTicket.RESUELTO, fechaLimite
            );
            
            String descripcionFrecuencia = obtenerDescripcionFrecuencia(frecuencia, hora, minutos);
            
            return String.format("Hay %d tickets pendientes de cierre automático. " +
                    "Configuración: %d días después de resolución, ejecutándose %s", 
                    ticketsParaCerrar.size(), diasParaCierre, descripcionFrecuencia);
                    
        } catch (Exception e) {
            logger.error("Error al obtener información de próximo cierre", e);
            return "Error al obtener información";
        }
    }
    
    private String obtenerDescripcionFrecuencia(String frecuencia, int hora, int minutos) {
        switch (frecuencia.toUpperCase()) {
            case "CADA_HORA":
                return "cada hora";
            case "CADA_6_HORAS":
                return "cada 6 horas";
            case "DIARIO":
            default:
                return String.format("diariamente a las %02d:%02d", hora, minutos);
        }
    }


@Transactional
public void ejecutarCierreAutomaticoManualCompleto() {
    logger.info("Ejecutando cierre automático manual - TODOS los tickets resueltos");
    
    try {
        // Verificar si el cierre automático está habilitado
        boolean cierreAutomaticoHabilitado = configuracionService.obtenerValorBooleano(
            "CIERRE_AUTOMATICO_HABILITADO", true
        );
        
        if (!cierreAutomaticoHabilitado) {
            logger.info("Cierre automático deshabilitado por configuración");
            return;
        }
        
        // Buscar TODOS los tickets resueltos (sin filtro de fecha)
        List<Ticket> ticketsParaCerrar = ticketRepository.findByEstado(EstadoTicket.RESUELTO, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        logger.info("Encontrados {} tickets resueltos para cerrar manualmente", ticketsParaCerrar.size());
        
        // Procesar cada ticket
        int ticketsCerrados = 0;
        int errores = 0;
        
        for (Ticket ticket : ticketsParaCerrar) {
            try {
                cerrarTicketAutomaticamente(ticket, 0); // 0 días indica cierre manual
                ticketsCerrados++;
                logger.info("Ticket {} cerrado manualmente", ticket.getNumeroTicket());
            } catch (Exception e) {
                errores++;
                logger.error("Error al cerrar manualmente el ticket {}: {}", 
                    ticket.getNumeroTicket(), e.getMessage(), e);
            }
        }
        
        logger.info("Proceso manual completado. Tickets cerrados: {}, Errores: {}", ticketsCerrados, errores);
        
        // Actualizar estadísticas
        actualizarEstadisticasCierreAutomatico(ticketsCerrados);
        
    } catch (Exception e) {
        logger.error("Error en el proceso manual de cierre automático de tickets", e);
    }
}
}