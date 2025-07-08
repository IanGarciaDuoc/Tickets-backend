package com.tickets.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import com.tickets.backend.models.EstadoTicket;
import com.tickets.backend.models.HistorialCambio;
import com.tickets.backend.models.Ticket;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.TicketRepository;
import com.tickets.backend.repository.UsuarioRepository;

@Service
public class CierreTareasService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private ConfiguracionSistemaService configuracionService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
  
    
    // Ejecutar diariamente a una hora específica
    @Scheduled(cron = "0 0 1 * * ?") // Ejemplo: 1:00 AM todos los días
    public void cerrarTicketsResueltos() {
        // Verificar si el cierre automático está habilitado
        boolean cierreAutomaticoHabilitado = configuracionService.obtenerValorBooleano("CIERRE_AUTOMATICO_HABILITADO", true);
        
        if (!cierreAutomaticoHabilitado) {
            return; // Si no está habilitado, salir del método
        }
        
        // Obtener días de espera desde configuración
        int diasEspera = configuracionService.obtenerValorNumerico("DIAS_PARA_CIERRE_AUTOMATICO", 2);
        
        // Calcular fecha límite
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasEspera);
        
        // Buscar tickets resueltos con fecha de resolución anterior a la fecha límite
        List<Ticket> ticketsParaCerrar = ticketRepository.findByEstadoAndFechaResolucionLessThan(
            EstadoTicket.RESUELTO, fechaLimite);
        
        // Obtener usuario sistema para registrar cambios automáticos
        Usuario usuarioSistema = obtenerUsuarioSistema();
        
        // Cerrar cada ticket
        for (Ticket ticket : ticketsParaCerrar) {
            ticket.setEstado(EstadoTicket.CERRADO);
            ticket.setFechaCierre(LocalDateTime.now());
            ticket.setFechaActualizacion(LocalDateTime.now());
            
            // Crear el historial de cambio
            HistorialCambio cambio = crearHistorialCambio(ticket, usuarioSistema, "estado", 
                EstadoTicket.RESUELTO.toString(), EstadoTicket.CERRADO.toString());
            
            // Guardar ticket
            ticketRepository.save(ticket);
            
            // Opcionalmente, enviar notificación
            //emailService.enviarNotificacionCierreAutomatico(ticket);
        }
    }
    
    private HistorialCambio crearHistorialCambio(Ticket ticket, Usuario usuario, String campo, String valorAnterior, String valorNuevo) {
        HistorialCambio cambio = new HistorialCambio();
        cambio.setTicket(ticket);
        cambio.setUsuario(usuario);
        cambio.setCampoModificado(campo);
        cambio.setValorAnterior(valorAnterior);
        cambio.setValorNuevo(valorNuevo);
        cambio.setFechaCambio(LocalDateTime.now());
        
        return cambio;
    }
    
    /**
     * Obtiene o crea un usuario de sistema para acciones automáticas
     */
    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findByEmail("sistema@tickets.com")
            .orElseGet(() -> {
                // Si no existe, crear un usuario de sistema
                Usuario sistema = new Usuario();
                sistema.setNombre("Sistema");
                sistema.setApellido("Automático");
                sistema.setEmail("sistema@tickets.com");
                sistema.setPassword("sistema_password"); // Idealmente encriptada
                sistema.setActivo(true);
                return usuarioRepository.save(sistema);
            });
    }
}