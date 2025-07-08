package com.tickets.backend.service;

import com.tickets.backend.dto.ComentarioDto;
import com.tickets.backend.dto.TicketDto;
import com.tickets.backend.exceptions.ResourceNotFoundException;
import com.tickets.backend.models.*;
import com.tickets.backend.repository.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import java.util.Arrays for asList method
import java.util.Arrays;

import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private HistorialCambioRepository historialCambioRepository;

    @Autowired
    private SLARepository slaRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private SupervisorTecnicoService supervisorTecnicoService;

    @Autowired
    private ConfiguracionSistemaService configuracionService;


  @Transactional
public Ticket crearTicket(TicketDto ticketDto, String emailUsuario) {
    Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    Categoria categoria = categoriaRepository.findById(ticketDto.getCategoriaId())
            .orElseThrow(() -> new ResourceNotFoundException("La categoría con ID " + ticketDto.getCategoriaId() + " no existe"));
    
    // ← VALIDACIÓN AGREGADA: Verificar que subcategoriaId no sea null
    if (ticketDto.getSubcategoriaId() == null) {
        throw new IllegalArgumentException("La subcategoría es obligatoria");
    }
    
    Subcategoria subcategoria = subcategoriaRepository.findById(ticketDto.getSubcategoriaId())
            .orElseThrow(() -> new ResourceNotFoundException("La subcategoría con ID " + ticketDto.getSubcategoriaId() + " no existe"));
    
    // Validación adicional: verificar que la subcategoría pertenezca a la categoría
    if (!subcategoria.getCategoria().getId().equals(categoria.getId())) {
        throw new IllegalArgumentException("La subcategoría no pertenece a la categoría seleccionada");
    }
    
    SLA sla = null;
    if (ticketDto.getPrioridad() != null) {
        sla = slaRepository.findByPrioridadAndActivoTrue(ticketDto.getPrioridad())
                .orElse(null);
    }
    
    Ticket ticket = new Ticket();
    ticket.setTitulo(ticketDto.getTitulo());
    ticket.setDescripcion(ticketDto.getDescripcion());
    ticket.setEstado(EstadoTicket.NUEVO);
    ticket.setPrioridad(ticketDto.getPrioridad());
    ticket.setCategoria(categoria);
    ticket.setSubcategoria(subcategoria);  // Ya no puede ser null
    ticket.setUsuarioCreador(usuario);
    ticket.setFechaCreacion(LocalDateTime.now());
    ticket.setFechaActualizacion(LocalDateTime.now());
    ticket.setSla(sla);
    
    // Generar y asignar el número correlativo
    ticket.setNumeroTicket(generarNumeroTicket());
    
    // Guardar el ticket
    Ticket ticketGuardado = ticketRepository.save(ticket);
    
    // Registrar en el historial
    registrarCambio(ticketGuardado, usuario, "estado", null, EstadoTicket.NUEVO.toString());
    
    // Enviar notificaciones por correo
    emailService.enviarNotificacionTicketCreado(ticketGuardado);
    
    return ticketGuardado;
}

// Método para generar el número correlativo
    private String generarNumeroTicket() {
        log.info("Iniciando generación de número de ticket");
        
        // Obtener configuración del prefijo (por defecto "TK-")
        String prefijo = "TK-";
        
        Optional<ConfiguracionSistema> configPrefijo = configuracionService.obtenerPorClave("PREFIJO_NUMERO_TICKET");
        if (configPrefijo.isPresent() && configPrefijo.get().getValor() != null && !configPrefijo.get().getValor().isEmpty()) {
            prefijo = configPrefijo.get().getValor();
            log.info("Prefijo configurado encontrado: {}", prefijo);
        } else {
            log.warn("No se encontró configuración para PREFIJO_NUMERO_TICKET, usando valor por defecto: {}", prefijo);
        }
        
        // Obtener configuración de dígitos (por defecto 6)
        int digitos = 6;
        Optional<ConfiguracionSistema> configDigitos = configuracionService.obtenerPorClave("DIGITOS_NUMERO_TICKET");
        if (configDigitos.isPresent() && configDigitos.get().getValor() != null && !configDigitos.get().getValor().isEmpty()) {
            try {
                digitos = Integer.parseInt(configDigitos.get().getValor());
                // Asegurar que esté en rango válido
                if (digitos < 1) {
                    log.warn("Valor de dígitos configurado es menor que 1, ajustando a 1");
                    digitos = 1;
                }
                if (digitos > 10) {
                    log.warn("Valor de dígitos configurado es mayor que 10, ajustando a 10");
                    digitos = 10;
                }
                log.info("Cantidad de dígitos configurada: {}", digitos);
            } catch (NumberFormatException e) {
                log.error("Error al parsear el número de dígitos configurado: {}", e.getMessage());
                log.info("Usando cantidad de dígitos por defecto: {}", digitos);
            }
        } else {
            log.warn("No se encontró configuración para DIGITOS_NUMERO_TICKET, usando valor por defecto: {}", digitos);
        }
        
        // Obtener el último ticket
        Ticket ultimoTicket = ticketRepository.findTopByOrderByIdDesc().orElse(null);
        
        int ultimoNumero = 0;
        if (ultimoTicket != null && ultimoTicket.getNumeroTicket() != null) {
            try {
                // Extraer número desde la posición del prefijo anterior
                String numeroTicketAnterior = ultimoTicket.getNumeroTicket();
                log.info("Último número de ticket encontrado: {}", numeroTicketAnterior);
                
                // Intentar encontrar la parte numérica independientemente del prefijo
                String numeroStr = numeroTicketAnterior.replaceAll("[^0-9]", "");
                
                if (!numeroStr.isEmpty()) {
                    ultimoNumero = Integer.parseInt(numeroStr);
                    log.info("Número extraído del último ticket: {}", ultimoNumero);
                } else {
                    log.warn("No se pudo extraer un número del último ticket, usando 0");
                }
            } catch (Exception e) {
                log.error("Error al extraer número del último ticket: {}", e.getMessage());
                log.info("Usando número inicial 0");
                ultimoNumero = 0;
            }
        } else {
            log.info("No se encontraron tickets previos, iniciando desde 0");
        }
        
        // Incrementar para el nuevo ticket
        ultimoNumero++;
        
        // Formatear el número con la cantidad de dígitos configurada
        String numeroFormateado = String.format("%0" + digitos + "d", ultimoNumero);
        String numeroTicketCompleto = prefijo + numeroFormateado;
        
        log.info("Número de ticket generado: {}", numeroTicketCompleto);
        return numeroTicketCompleto;
    }
    

    @Transactional
    public Ticket actualizarTicket(Long id, TicketDto ticketDto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (ticketDto.getTitulo() != null && !ticketDto.getTitulo().equals(ticket.getTitulo())) {
            String valorAnterior = ticket.getTitulo();
            ticket.setTitulo(ticketDto.getTitulo());
            registrarCambio(ticket, usuario, "titulo", valorAnterior, ticketDto.getTitulo());
        }

        if (ticketDto.getDescripcion() != null && !ticketDto.getDescripcion().equals(ticket.getDescripcion())) {
            String valorAnterior = ticket.getDescripcion();
            ticket.setDescripcion(ticketDto.getDescripcion());
            registrarCambio(ticket, usuario, "descripcion", valorAnterior, ticketDto.getDescripcion());
        }

        if (ticketDto.getEstado() != null && ticket.getEstado() != ticketDto.getEstado()) {
            EstadoTicket valorAnterior = ticket.getEstado();
            ticket.setEstado(ticketDto.getEstado());
            
            // Si el estado cambia a resuelto o cerrado, registrar la fecha
            if (ticketDto.getEstado() == EstadoTicket.RESUELTO) {
                ticket.setFechaResolucion(LocalDateTime.now());
            } else if (ticketDto.getEstado() == EstadoTicket.CERRADO) {
                ticket.setFechaCierre(LocalDateTime.now());
            }
            
            registrarCambio(ticket, usuario, "estado", valorAnterior.toString(), ticketDto.getEstado().toString());
        }

        if (ticketDto.getPrioridad() != null && ticket.getPrioridad() != ticketDto.getPrioridad()) {
            PrioridadTicket valorAnterior = ticket.getPrioridad();
            ticket.setPrioridad(ticketDto.getPrioridad());
            
            // Actualizar SLA según la prioridad
            slaRepository.findByPrioridadAndActivoTrue(ticketDto.getPrioridad())
                    .ifPresent(ticket::setSla);
            
            registrarCambio(ticket, usuario, "prioridad", valorAnterior != null ? valorAnterior.toString() : null, 
                    ticketDto.getPrioridad().toString());
        }

        if (ticketDto.getCategoriaId() != null && 
            (ticket.getCategoria() == null || !ticketDto.getCategoriaId().equals(ticket.getCategoria().getId()))) {
            
            String valorAnterior = ticket.getCategoria() != null ? ticket.getCategoria().getNombre() : null;
            
            Categoria categoria = categoriaRepository.findById(ticketDto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            
            ticket.setCategoria(categoria);
            registrarCambio(ticket, usuario, "categoria", valorAnterior, categoria.getNombre());
        }

        if (ticketDto.getSubcategoriaId() != null && 
            (ticket.getSubcategoria() == null || !ticketDto.getSubcategoriaId().equals(ticket.getSubcategoria().getId()))) {
            
            String valorAnterior = ticket.getSubcategoria() != null ? ticket.getSubcategoria().getNombre() : null;
            
            Subcategoria subcategoria = subcategoriaRepository.findById(ticketDto.getSubcategoriaId())
                    .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
            
            ticket.setSubcategoria(subcategoria);
            registrarCambio(ticket, usuario, "subcategoria", valorAnterior, subcategoria.getNombre());
        }

        if (ticketDto.getTecnicoAsignadoId() != null && 
            (ticket.getTecnicoAsignado() == null || !ticketDto.getTecnicoAsignadoId().equals(ticket.getTecnicoAsignado().getId()))) {
            
            String valorAnterior = ticket.getTecnicoAsignado() != null ? 
                    ticket.getTecnicoAsignado().getNombre() + " " + ticket.getTecnicoAsignado().getApellido() : null;
            
            Usuario tecnico = usuarioRepository.findById(ticketDto.getTecnicoAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
            
            ticket.setTecnicoAsignado(tecnico);
            
            // Si se asigna técnico y el estado es NUEVO, cambiarlo a ASIGNADO
            if (ticket.getEstado() == EstadoTicket.NUEVO) {
                ticket.setEstado(EstadoTicket.ASIGNADO);
                registrarCambio(ticket, usuario, "estado", EstadoTicket.NUEVO.toString(), EstadoTicket.ASIGNADO.toString());
            }
            
            registrarCambio(ticket, usuario, "tecnico", valorAnterior, 
                    tecnico.getNombre() + " " + tecnico.getApellido());
        }

        ticket.setFechaActualizacion(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

@   Transactional
    public Ticket asignarTicket(Long ticketId, Long tecnicoId, String emailUsuario) {
    Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    
    Usuario tecnico = usuarioRepository.findById(tecnicoId)
            .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
    
    // NUEVA VALIDACIÓN: Verificar que el técnico pertenece a la categoría del ticket
    if (ticket.getCategoria() != null && tecnico.getCategoria() != null) {
        if (!ticket.getCategoria().getId().equals(tecnico.getCategoria().getId())) {
            throw new IllegalArgumentException("El técnico no pertenece a la categoría del ticket");
        }
    } else {
        throw new IllegalArgumentException("El ticket y el técnico deben tener categorías asignadas");
    }
    
    // NUEVA VALIDACIÓN: Verificar permisos de asignación
    if (!supervisorTecnicoService.puedeAsignarTicket(usuario.getId(), tecnicoId)) {
        throw new IllegalArgumentException("No tiene permisos para asignar este ticket al técnico especificado");
    }
    
    String valorAnterior = ticket.getTecnicoAsignado() != null ? 
            ticket.getTecnicoAsignado().getNombre() + " " + ticket.getTecnicoAsignado().getApellido() : null;
    
    ticket.setTecnicoAsignado(tecnico);
    
    // Si el estado es NUEVO, cambiarlo a ASIGNADO
    if (ticket.getEstado() == EstadoTicket.NUEVO) {
        ticket.setEstado(EstadoTicket.ASIGNADO);
        registrarCambio(ticket, usuario, "estado", EstadoTicket.NUEVO.toString(), EstadoTicket.ASIGNADO.toString());
    }
    
    ticket.setFechaActualizacion(LocalDateTime.now());
    registrarCambio(ticket, usuario, "tecnico", valorAnterior, 
            tecnico.getNombre() + " " + tecnico.getApellido());
    
    Ticket ticketActualizado = ticketRepository.save(ticket);
    
    // Enviar correo de notificación de asignación
    emailService.enviarNotificacionTicketAsignado(ticketActualizado);
    
    return ticketActualizado;

}

    public List<Usuario> obtenerTecnicosDisponiblesParaTicket(Long ticketId, String emailUsuario) {
    Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    
    if (ticket.getCategoria() == null) {
        throw new IllegalArgumentException("El ticket debe tener una categoría asignada");
    }
    
    // Usar el nuevo servicio para obtener técnicos disponibles
    return supervisorTecnicoService.obtenerTecnicosDisponibles(usuario.getId(), ticket.getCategoria().getId());
}

    @Transactional
    public Ticket tomarTicket(Long ticketId, String emailUsuario) {
        Usuario tecnico = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        String valorAnterior = ticket.getTecnicoAsignado() != null ? 
                ticket.getTecnicoAsignado().getNombre() + " " + ticket.getTecnicoAsignado().getApellido() : null;
        
        ticket.setTecnicoAsignado(tecnico);
        
        // Si el estado es NUEVO, cambiarlo a ASIGNADO
        if (ticket.getEstado() == EstadoTicket.NUEVO) {
            ticket.setEstado(EstadoTicket.ASIGNADO);
            registrarCambio(ticket, tecnico, "estado", EstadoTicket.NUEVO.toString(), EstadoTicket.ASIGNADO.toString());
        }
        
        ticket.setFechaActualizacion(LocalDateTime.now());
        registrarCambio(ticket, tecnico, "tecnico", valorAnterior, 
                tecnico.getNombre() + " " + tecnico.getApellido());
        
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket cambiarEstado(Long ticketId, EstadoTicket nuevoEstado, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        EstadoTicket estadoAnterior = ticket.getEstado();
        ticket.setEstado(nuevoEstado);
        
        // Registrar fechas especiales según el estado
        if (nuevoEstado == EstadoTicket.RESUELTO) {
            ticket.setFechaResolucion(LocalDateTime.now());
        } else if (nuevoEstado == EstadoTicket.CERRADO) {
            ticket.setFechaCierre(LocalDateTime.now());
        }
        
        ticket.setFechaActualizacion(LocalDateTime.now());
        registrarCambio(ticket, usuario, "estado", estadoAnterior.toString(), nuevoEstado.toString());
        
        Ticket ticketActualizado = ticketRepository.save(ticket);
        
        // Si el nuevo estado es EN_PROGRESO, enviar notificación
        if (nuevoEstado == EstadoTicket.EN_PROGRESO) {
            emailService.enviarNotificacionTicketEnProgreso(ticketActualizado);
        }
        
        return ticketActualizado;
    }
    @Transactional
    public Comentario agregarComentario(Long ticketId, ComentarioDto comentarioDto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        Comentario comentario = new Comentario();
        comentario.setTicket(ticket);
        comentario.setUsuario(usuario);
        comentario.setContenido(comentarioDto.getContenido());
        comentario.setFechaCreacion(LocalDateTime.now());
        comentario.setEsPrivado(comentarioDto.isEsPrivado());
        
        ticket.setFechaActualizacion(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        return comentarioRepository.save(comentario);
    }

    public Ticket obtenerTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    public Page<Ticket> obtenerTicketsPorUsuario(String email, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return ticketRepository.findByUsuarioCreador(usuario, pageable);
    }

    public Page<Ticket> obtenerTicketsPorTecnico(String email, Pageable pageable) {
        Usuario tecnico = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
        
        return ticketRepository.findByTecnicoAsignado(tecnico, pageable);
    }

    public Page<Ticket> obtenerTicketsSinAsignar(Pageable pageable) {
        return ticketRepository.findByTecnicoAsignadoIsNull(pageable);
    }

    public Page<Ticket> obtenerTicketsPorEstado(EstadoTicket estado, Pageable pageable) {
        return ticketRepository.findByEstado(estado, pageable);
    }

    public Page<Ticket> buscarTickets(String keyword, Pageable pageable) {
        return ticketRepository.searchByKeyword(keyword, pageable);
    }

    public List<Comentario> obtenerComentarios(Long ticketId, boolean incluirPrivados) {
        if (incluirPrivados) {
            return comentarioRepository.findByTicketIdOrderByFechaCreacionDesc(ticketId);
        } else {
            return comentarioRepository.findByTicketIdAndEsPrivadoFalse(ticketId);
        }
    }

    public List<HistorialCambio> obtenerHistorialCambios(Long ticketId) {
        return historialCambioRepository.findByTicketIdOrderByFechaCambioDesc(ticketId);
    }

    private void registrarCambio(Ticket ticket, Usuario usuario, String campo, String valorAnterior, String valorNuevo) {
        HistorialCambio cambio = new HistorialCambio();
        cambio.setTicket(ticket);
        cambio.setUsuario(usuario);
        cambio.setCampoModificado(campo);
        cambio.setValorAnterior(valorAnterior);
        cambio.setValorNuevo(valorNuevo);
        cambio.setFechaCambio(LocalDateTime.now());
        
        historialCambioRepository.save(cambio);
    }

    public Long contarTicketsPorEstado(EstadoTicket estado) {
    return ticketRepository.countByEstado(estado);
}

    public Long contarTicketsPorPrioridad(PrioridadTicket... prioridades) {
        return ticketRepository.countByPrioridadIn(Arrays.asList(prioridades));
    }

    public List<Ticket> obtenerTicketsRecientes(int cantidad) {
        PageRequest pageRequest = PageRequest.of(0, cantidad, Sort.by("fechaCreacion").descending());
        return ticketRepository.findAll(pageRequest).getContent();
    }

    public List<Map<String, Object>> obtenerTicketsPorMes() {
        // Implementación para obtener tickets agrupados por mes
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        // Obtener los últimos 6 meses
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
        
        for (int i = 5; i >= 0; i--) {
            LocalDate fechaMes = fechaActual.minusMonths(i);
            String nombreMes = fechaMes.format(formatter);
            
            // Obtener la cantidad de tickets para ese mes
            YearMonth yearMonth = YearMonth.from(fechaMes);
            LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            long cantidad = ticketRepository.countByFechaCreacionBetween(inicio, fin);
            
            Map<String, Object> datoMes = new HashMap<>();
            datoMes.put("mes", nombreMes);
            datoMes.put("cantidad", cantidad);
            
            resultado.add(datoMes);
        }
        
        return resultado;
    }

    public List<Map<String, Object>> obtenerTicketsPorCategoria() {
        // Obtener categorías y contar tickets para cada una
        List<Categoria> categorias = categoriaRepository.findByActivoTrue();
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (Categoria categoria : categorias) {
            long cantidad = ticketRepository.countByCategoria(categoria);
            
            // Solo incluir categorías con tickets
            if (cantidad > 0) {
                Map<String, Object> dato = new HashMap<>();
                dato.put("categoria", categoria.getNombre());
                dato.put("cantidad", cantidad);
                resultado.add(dato);
            }
        }
        
        return resultado;
    }


    public Page<Ticket> obtenerTodosTicketsConFiltros(
        String estado,
        String prioridad,
        Long categoriaId,
        Long tecnicoId,
        Long usuarioId,
        String fechaDesde,
        String fechaHasta,
        String busqueda,
        Pageable pageable
    ) {
        // Convertir fechas usando método robusto de parseado
        LocalDateTime fechaDesdeParsed = parseFecha(fechaDesde, true);  // true = inicio del día
        LocalDateTime fechaHastaParsed = parseFecha(fechaHasta, false); // false = fin del día
        
        return ticketRepository.buscarConFiltros(
            estado, prioridad, categoriaId, tecnicoId, usuarioId, 
            fechaDesdeParsed, fechaHastaParsed, busqueda, pageable
        );
    }

    /**
     * Parsea una fecha en diferentes formatos, soportando dd-MM-yyyy y yyyy-MM-dd
     * @param fechaStr String con la fecha a parsear
     * @param inicioDelDia true para establecer hora a 00:00:00, false para 23:59:59
     * @return LocalDateTime parseado o null si la fecha es inválida
     */
    private LocalDateTime parseFecha(String fechaStr, boolean inicioDelDia) {

        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Primero intentar con formato ISO (si ya viene con tiempo)
            if (fechaStr.contains("T")) {
                return LocalDateTime.parse(fechaStr);
            }
            
            // Intentar primero con formato dd-MM-yyyy
            if (fechaStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate fecha = LocalDate.parse(fechaStr, formatter);
                return inicioDelDia ? fecha.atStartOfDay() : fecha.atTime(23, 59, 59);
            }
            
            // Intentar con formato yyyy-MM-dd
            if (fechaStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate fecha = LocalDate.parse(fechaStr);
                return inicioDelDia ? fecha.atStartOfDay() : fecha.atTime(23, 59, 59);
            }
            
            // Si no coincide con patrones conocidos, intentar directamente (puede lanzar excepción)
            LocalDate fecha = LocalDate.parse(fechaStr);
            return inicioDelDia ? fecha.atStartOfDay() : fecha.atTime(23, 59, 59);
            
        } catch (Exception e) {
            // Registrar el error para depuración
            System.err.println("Error al parsear fecha [" + fechaStr + "]: " + e.getMessage());
            
            // Retornar null si no se puede parsear
            return null;
        }
    }
    public void asignarTicketsMasivo(List<Long> ticketIds, Long tecnicoId, String email) throws AccessDeniedException {
        // Verificar que el usuario tenga permisos de administrador
    
    
    
        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Verificar si el usuario tiene rol de administrador
        boolean esAdmin = false;
        for (Rol rol : admin.getRoles()) {
            if (rol.getNombre().equals("ROLE_ADMIN")) {
                esAdmin = true;
                break;
            }
        }

        if (!esAdmin) {
            throw new AccessDeniedException("Solo los administradores pueden realizar asignaciones masivas");
        }

        // Obtener el técnico al que se asignarán los tickets
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado con ID: " + tecnicoId));

        // Verificar si el técnico tiene al menos un rol de soporte o técnico
        boolean esTecnico = false;
        for (Rol rol : tecnico.getRoles()) {
            if (rol.getNombre().equals("ROLE_TECNICO") || rol.getNombre().equals("ROLE_SOPORTE")) {
                esTecnico = true;
                break;
            }
        }

        if (!esTecnico) {
            throw new BadRequestException("El usuario seleccionado no tiene rol de técnico");
        }

        // Asignar cada ticket
        for (Long ticketId : ticketIds) {
            try {
                // Obtener el ticket
                Ticket ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

                // Guardar el estado anterior para el historial
                EstadoTicket estadoAnterior = ticket.getEstado();
                Usuario tecnicoAnterior = ticket.getTecnicoAsignado();

                // Actualizar el ticket
                ticket.setTecnicoAsignado(tecnico);

                // Si el ticket estaba en estado NUEVO, cambiarlo a ASIGNADO
                if (ticket.getEstado() == EstadoTicket.NUEVO) {
                    ticket.setEstado(EstadoTicket.ASIGNADO);
                }

                // Actualizar la fecha de modificación
                ticket.setFechaActualizacion(LocalDateTime.now());

                // Guardar el ticket actualizado
                ticketRepository.save(ticket);

                // Registrar el cambio en el historial
                HistorialCambio historial = new HistorialCambio();
                historial.setTicket(ticket);
                historial.setUsuario(admin);
                historial.setFechaCambio(LocalDateTime.now());
                historial.setCampoModificado("asignacion_masiva");
                historial.setValorAnterior(tecnicoAnterior == null ? "Sin asignar" : 
                            tecnicoAnterior.getNombre() + " " + tecnicoAnterior.getApellido());
                historial.setValorNuevo(tecnico.getNombre() + " " + tecnico.getApellido());
                
                // Crear la descripción del cambio
                String descripcion = "Asignación masiva: ";
                if (tecnicoAnterior == null) {
                    descripcion += "Ticket asignado a " + tecnico.getNombre() + " " + tecnico.getApellido();
                } else {
                    descripcion += "Técnico cambiado de " + tecnicoAnterior.getNombre() + " " + tecnicoAnterior.getApellido() 
                            + " a " + tecnico.getNombre() + " " + tecnico.getApellido();
                }
                
                if (estadoAnterior != ticket.getEstado()) {
                    descripcion += ". Estado cambiado de " + estadoAnterior + " a " + ticket.getEstado();
                }
                
            
                historialCambioRepository.save(historial);

                // Opcional: Enviar notificación al técnico
            // notificarAsignacionTicket(ticket, tecnico);

            } catch (ResourceNotFoundException e) {
                // Registrar el error pero continuar con los demás tickets
                log.error("Error al asignar ticket con ID {}: {}", ticketId, e.getMessage());
            }
        }
    }
    public void cambiarEstadoMasivo(List<Long> ticketIds, EstadoTicket nuevoEstado, String email) {
        // Verificar que el usuario tenga permisos de administrador
        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // Verificar si el usuario tiene rol de administrador
        boolean esAdmin = false;
        for (Rol rol : admin.getRoles()) {
            if (rol.getNombre().equals("ROLE_ADMIN")) {
                esAdmin = true;
                break;
            }
        }

        if (!esAdmin) {
            throw new RuntimeException("Solo los administradores pueden realizar cambios masivos de estado");
        }

        // Cambiar el estado de cada ticket
        for (Long ticketId : ticketIds) {
            try {
                // Obtener el ticket
                Ticket ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + ticketId));

                // Guardar el estado anterior para el historial
                EstadoTicket estadoAnterior = ticket.getEstado();

                // Si ya está en el mismo estado, no hacer nada
                if (estadoAnterior == nuevoEstado) {
                    continue;
                }

                // Validar la transición de estado
                boolean transicionValida = validarTransicionEstado(estadoAnterior, nuevoEstado);
                if (!transicionValida) {
                    log.warn("Transición de estado inválida para ticket {}: de {} a {}", 
                            ticketId, estadoAnterior, nuevoEstado);
                    continue;
                }

                // Actualizar el estado del ticket
                ticket.setEstado(nuevoEstado);

                // Actualizar fechas según el nuevo estado
                actualizarFechasSegunEstado(ticket, nuevoEstado);

                // Actualizar la fecha de modificación
                ticket.setFechaActualizacion(LocalDateTime.now());

                // Guardar el ticket actualizado
                ticketRepository.save(ticket);

                // Registrar el cambio en el historial
                HistorialCambio historial = new HistorialCambio();
                historial.setTicket(ticket);
                historial.setUsuario(admin);
                historial.setFechaCambio(LocalDateTime.now());
                historial.setCampoModificado("estado");
                historial.setValorAnterior(estadoAnterior.toString());
                historial.setValorNuevo(nuevoEstado.toString());
                historialCambioRepository.save(historial);

                // Opcional: Enviar notificación al usuario y/o técnico
                // notificarCambioEstado(ticket, estadoAnterior, nuevoEstado);

            } catch (ResourceNotFoundException e) {
                // Registrar el error pero continuar con los demás tickets
                log.error("Error al cambiar estado del ticket con ID {}: {}", ticketId, e.getMessage());
            }
        }
    }

    /**
     * Valida si una transición de estado es válida
     */
    private boolean validarTransicionEstado(EstadoTicket estadoActual, EstadoTicket nuevoEstado) {
        // Implementar lógica de validación de estados
        // Por ejemplo, no permitir cambiar directamente de NUEVO a RESUELTO
        
        // Ejemplo simple: permitir todas las transiciones excepto algunas específicas
        if (estadoActual == EstadoTicket.CERRADO) {
            // Un ticket cerrado no se puede reabrir automáticamente
            return nuevoEstado == EstadoTicket.CERRADO;
        }
        
        if (estadoActual == EstadoTicket.NUEVO && 
            (nuevoEstado == EstadoTicket.RESUELTO || nuevoEstado == EstadoTicket.CERRADO)) {
            // No permitir saltar directamente de NUEVO a RESUELTO o CERRADO
            return false;
        }
        
        return true;
    }

    /**
     * Actualiza las fechas del ticket según el nuevo estado
     */
    private void actualizarFechasSegunEstado(Ticket ticket, EstadoTicket nuevoEstado) {
        LocalDateTime ahora = LocalDateTime.now();
        
        if (nuevoEstado == EstadoTicket.RESUELTO) {
            ticket.setFechaResolucion(ahora);
        }
        
        if (nuevoEstado == EstadoTicket.CERRADO) {
            if (ticket.getFechaResolucion() == null) {
                ticket.setFechaResolucion(ahora);
            }
            ticket.setFechaCierre(ahora);
        }
    }

    public Integer obtenerUltimoNumeroCorrelativo() {
        log.info("Obteniendo último número correlativo");
        
        // Obtener el último ticket creado
        Ticket ultimoTicket = ticketRepository.findTopByOrderByIdDesc().orElse(null);
        
        if (ultimoTicket == null || ultimoTicket.getNumeroTicket() == null) {
            log.info("No se encontraron tickets, el último correlativo es 0");
            return 0;
        }
        
        // Extraer el número correlativo del número de ticket
        String numeroTicket = ultimoTicket.getNumeroTicket();
        log.info("Último número de ticket: {}", numeroTicket);
        
        try {
            // Extraer solo los dígitos (independiente del prefijo)
            String numeroStr = numeroTicket.replaceAll("[^0-9]", "");
            
            if (!numeroStr.isEmpty()) {
                Integer ultimoNumero = Integer.parseInt(numeroStr);
                log.info("Último número correlativo: {}", ultimoNumero);
                return ultimoNumero;
            } else {
                log.warn("No se pudo extraer número del ticket: {}", numeroTicket);
                return 0;
            }
        } catch (Exception e) {
            log.error("Error al extraer el número correlativo: {}", e.getMessage());
            return 0;
        }
    }
    @Transactional
    public void resetearCorrelativo() {
        log.info("Reseteando correlativo de tickets");
        
        try {
            // Guardamos una configuración especial para indicar que el correlativo se ha reseteado
            // Esta configuración se usará en el método generarNumeroTicket
            ConfiguracionSistema configReset = new ConfiguracionSistema();
            configReset.setClave("TICKET_CORRELATIVO_RESETEADO");
            configReset.setValor("true");
            configReset.setDescripcion("Indica que el correlativo de tickets ha sido reseteado");
            configReset.setTipo("TICKETS");
            
            configuracionService.actualizarConfiguracion("TICKET_CORRELATIVO_RESETEADO", "true");
            log.info("Configuración de reseteo guardada correctamente");
            
            // También guardamos el último correlativo antes del reseteo
            int ultimoCorrelativo = obtenerUltimoNumeroCorrelativo();
            configuracionService.actualizarConfiguracion("ULTIMO_CORRELATIVO_ANTES_RESET", 
                    String.valueOf(ultimoCorrelativo));
            log.info("Último correlativo guardado: {}", ultimoCorrelativo);
            
        } catch (Exception e) {
            log.error("Error al resetear correlativo: {}", e.getMessage());
            throw e;
        }
    }
    public Page<Ticket> obtenerTicketsAsignadosPorSupervisor(String emailTecnico, Pageable pageable) {
    Usuario tecnico = usuarioRepository.findByEmail(emailTecnico)
            .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
    
    // Obtener supervisores del técnico
    List<Usuario> supervisores = supervisorTecnicoService.obtenerSupervisoresDeTecnico(tecnico.getId());
    
    if (supervisores.isEmpty()) {
        // Si no tiene supervisores, devolver página vacía
        return Page.empty(pageable);
    }
    
    // Obtener tickets asignados al técnico por cualquiera de sus supervisores
    // (tickets que están asignados al técnico y fueron creados por sus supervisores o asignados por ellos)
    return ticketRepository.findByTecnicoAsignadoAndEstadoIn(
        tecnico, 
        Arrays.asList(EstadoTicket.ASIGNADO, EstadoTicket.EN_PROGRESO, EstadoTicket.EN_ESPERA), 
        pageable
    );
}
    
    }