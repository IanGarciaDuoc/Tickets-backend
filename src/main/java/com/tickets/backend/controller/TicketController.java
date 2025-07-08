package com.tickets.backend.controller;

import com.tickets.backend.dto.ComentarioDto;
import com.tickets.backend.dto.TicketDto;
import com.tickets.backend.dto.TicketResponseDto;
import com.tickets.backend.models.Categoria;
import com.tickets.backend.models.Comentario;
import com.tickets.backend.models.EstadoTicket;
import com.tickets.backend.models.HistorialCambio;
import com.tickets.backend.models.PrioridadTicket;
import com.tickets.backend.models.Ticket;
import com.tickets.backend.service.TicketService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tickets.backend.repository.CategoriaRepository;
import com.tickets.backend.repository.TicketRepository;


import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import org.springframework.security.access.AccessDeniedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;


    @PostMapping
    public ResponseEntity<TicketResponseDto> crearTicket(@Valid @RequestBody TicketDto ticketDto, Principal principal) {
        // Obtener el email del usuario autenticado
        String email = principal.getName();
        
        // Crear el ticket usando el servicio
        Ticket ticket = ticketService.crearTicket(ticketDto, email);
        
        // Convertir a DTO de respuesta
        TicketResponseDto responseDto = convertirAResponseDto(ticket);
        
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    private TicketResponseDto convertirAResponseDto(Ticket ticket) {
    TicketResponseDto dto = new TicketResponseDto();
    dto.setId(ticket.getId());
    dto.setNumeroTicket(ticket.getNumeroTicket());
    dto.setTitulo(ticket.getTitulo());
    dto.setDescripcion(ticket.getDescripcion());
    dto.setEstado(ticket.getEstado());
    dto.setPrioridad(ticket.getPrioridad());
    
    if (ticket.getCategoria() != null) {
        dto.setCategoriaId(ticket.getCategoria().getId());
        dto.setCategoriaNombre(ticket.getCategoria().getNombre());
    }
    
    if (ticket.getSubcategoria() != null) {
        dto.setSubcategoriaId(ticket.getSubcategoria().getId());
        dto.setSubcategoriaNombre(ticket.getSubcategoria().getNombre());
    }
    
    if (ticket.getUsuarioCreador() != null) {
        dto.setUsuarioCreadorId(ticket.getUsuarioCreador().getId());
        
        // Campos para compatibilidad con versiones anteriores
        dto.setUsuarioCreadorNombre(ticket.getUsuarioCreador().getNombre() + " " + ticket.getUsuarioCreador().getApellido());
        
        // Nuevos campos separados para compatibilidad con frontend
        dto.setUsuarioNombre(ticket.getUsuarioCreador().getNombre());
        dto.setUsuarioApellido(ticket.getUsuarioCreador().getApellido());
    }
    
    if (ticket.getTecnicoAsignado() != null) {
        dto.setTecnicoAsignadoId(ticket.getTecnicoAsignado().getId());
        
        // Campos para compatibilidad con versiones anteriores
        dto.setTecnicoAsignadoNombre(ticket.getTecnicoAsignado().getNombre() + " " + ticket.getTecnicoAsignado().getApellido());
        
        // Nuevos campos separados para compatibilidad con frontend
        dto.setTecnicoAsignadoNombreFirstOnly(ticket.getTecnicoAsignado().getNombre());
        dto.setTecnicoAsignadoApellido(ticket.getTecnicoAsignado().getApellido());
    }
    
    dto.setFechaCreacion(ticket.getFechaCreacion());
    dto.setFechaActualizacion(ticket.getFechaActualizacion());
    dto.setFechaResolucion(ticket.getFechaResolucion());
    dto.setFechaCierre(ticket.getFechaCierre());
    
    return dto;
}
    
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDto> actualizarTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketDto ticketDto,
            Authentication authentication) {
        
        String email = authentication.getName();
        Ticket ticketActualizado = ticketService.actualizarTicket(id, ticketDto, email);
        return ResponseEntity.ok(convertirAResponseDto(ticketActualizado));
    }

    @PutMapping("/{id}/asignar/{tecnicoId}")
    public ResponseEntity<TicketResponseDto> asignarTicket(
            @PathVariable Long id,
            @PathVariable Long tecnicoId,
            Authentication authentication) {
        
        String email = authentication.getName();
        Ticket ticketAsignado = ticketService.asignarTicket(id, tecnicoId, email);
        return ResponseEntity.ok(convertirAResponseDto(ticketAsignado));
    }

    @PutMapping("/{id}/tomar")
    public ResponseEntity<TicketResponseDto> tomarTicket(
            @PathVariable Long id,
            Authentication authentication) {
        
        String email = authentication.getName();
        Ticket ticketTomado = ticketService.tomarTicket(id, email);
        return ResponseEntity.ok(convertirAResponseDto(ticketTomado));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<TicketResponseDto> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        String email = authentication.getName();
        EstadoTicket nuevoEstado = EstadoTicket.valueOf(payload.get("estado"));
        Ticket ticketActualizado = ticketService.cambiarEstado(id, nuevoEstado, email);
        return ResponseEntity.ok(convertirAResponseDto(ticketActualizado));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Comentario> agregarComentario(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioDto comentarioDto,
            Authentication authentication) {
        
        String email = authentication.getName();
        Comentario nuevoComentario = ticketService.agregarComentario(id, comentarioDto, email);
        return new ResponseEntity<>(nuevoComentario, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> obtenerTicket(@PathVariable Long id) {
        Ticket ticket = ticketService.obtenerTicket(id);
        return ResponseEntity.ok(convertirAResponseDto(ticket));
    }

    @GetMapping("/mis-tickets")
    public ResponseEntity<Page<TicketResponseDto>> obtenerMisTickets(
            Pageable pageable,
            Authentication authentication) {
        
        String email = authentication.getName();
        Page<Ticket> tickets = ticketService.obtenerTicketsPorUsuario(email, pageable);
        
        // Convertir la página de tickets a una página de DTOs
        Page<TicketResponseDto> ticketsDto = tickets.map(this::convertirAResponseDto);
        
        return ResponseEntity.ok(ticketsDto);
    }

    @GetMapping("/mis-asignaciones")
    public ResponseEntity<Page<TicketResponseDto>> obtenerMisAsignaciones(
            Pageable pageable,
            Authentication authentication) {
        
        String email = authentication.getName();
        Page<Ticket> tickets = ticketService.obtenerTicketsPorTecnico(email, pageable);
        return ResponseEntity.ok(tickets.map(this::convertirAResponseDto));
    }

    @GetMapping("/sin-asignar")
    public ResponseEntity<Page<TicketResponseDto>> obtenerTicketsSinAsignar(Pageable pageable) {
        Page<Ticket> tickets = ticketService.obtenerTicketsSinAsignar(pageable);
        return ResponseEntity.ok(tickets.map(this::convertirAResponseDto));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<Page<TicketResponseDto>> obtenerTicketsPorEstado(
            @PathVariable EstadoTicket estado,
            Pageable pageable) {
        
        Page<Ticket> tickets = ticketService.obtenerTicketsPorEstado(estado, pageable);
        return ResponseEntity.ok(tickets.map(this::convertirAResponseDto));
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<TicketResponseDto>> buscarTickets(
            @RequestParam String keyword,
            Pageable pageable) {
        
        Page<Ticket> tickets = ticketService.buscarTickets(keyword, pageable);
        return ResponseEntity.ok(tickets.map(this::convertirAResponseDto));
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<Comentario>> obtenerComentarios(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean incluirPrivados) {
        
        List<Comentario> comentarios = ticketService.obtenerComentarios(id, incluirPrivados);
        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialCambio>> obtenerHistorialCambios(@PathVariable Long id) {
        List<HistorialCambio> historial = ticketService.obtenerHistorialCambios(id);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Contar tickets por cada estado
        Long nuevo = ticketRepository.countByEstado(EstadoTicket.NUEVO);
        Long asignado = ticketRepository.countByEstado(EstadoTicket.ASIGNADO);
        Long enProgreso = ticketRepository.countByEstado(EstadoTicket.EN_PROGRESO);
        Long enEspera = ticketRepository.countByEstado(EstadoTicket.EN_ESPERA);
        Long resuelto = ticketRepository.countByEstado(EstadoTicket.RESUELTO);
        Long cerrado = ticketRepository.countByEstado(EstadoTicket.CERRADO);
        
        // Obtener conteo de tickets urgentes (con prioridad ALTA o CRITICA)
        Long urgentes = ticketRepository.countByPrioridadIn(
            Arrays.asList(PrioridadTicket.ALTA, PrioridadTicket.CRITICA));
        
        estadisticas.put("nuevo", nuevo);
        estadisticas.put("asignado", asignado);
        estadisticas.put("enProgreso", enProgreso);
        estadisticas.put("enEspera", enEspera);
        estadisticas.put("resuelto", resuelto);
        estadisticas.put("cerrado", cerrado);
        estadisticas.put("urgentes", urgentes);
        
        return ResponseEntity.ok(estadisticas);
    }
    @GetMapping("/recientes")
    public ResponseEntity<List<TicketResponseDto>> obtenerTicketsRecientes() {
        // Obtener los 5 tickets más recientes
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("fechaCreacion").descending());
        Page<Ticket> ticketsPage = ticketRepository.findAll(pageRequest);
        List<Ticket> tickets = ticketsPage.getContent();
        
        // Convertir a DTOs
        List<TicketResponseDto> ticketsDto = tickets.stream()
            .map(this::convertirAResponseDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ticketsDto);
    }

    @GetMapping("/datos-mensuales")
    public ResponseEntity<List<Map<String, Object>>> obtenerDatosMensuales() {
        List<Map<String, Object>> datos = new ArrayList<>();
        
        // Obtener últimos 6 meses
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM").withLocale(new Locale("es", "ES"));
        
        for (int i = 5; i >= 0; i--) {
            LocalDate fechaMes = fechaActual.minusMonths(i);
            String nombreMes = fechaMes.format(formatter).toLowerCase();
            
            // Determinar inicio y fin del mes
            YearMonth yearMonth = YearMonth.from(fechaMes);
            LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            // Contar tickets creados en ese período
            long cantidad = ticketRepository.countByFechaCreacionBetween(inicio, fin);
            
            Map<String, Object> datoMes = new HashMap<>();
            datoMes.put("mes", nombreMes);
            datoMes.put("cantidad", cantidad);
            
            datos.add(datoMes);
        }
        
        return ResponseEntity.ok(datos);
    }

    @GetMapping("/datos-categoria")
    public ResponseEntity<List<Map<String, Object>>> obtenerDatosPorCategoria() {
        List<Map<String, Object>> datos = new ArrayList<>();
        
        // Obtener categorías activas
        List<Categoria> categorias = categoriaRepository.findByActivoTrue();
        
        for (Categoria categoria : categorias) {
            // Contar tickets para esta categoría
            long cantidad = ticketRepository.countByCategoria(categoria);
            
            // Solo incluir categorías con tickets
            if (cantidad > 0) {
                Map<String, Object> datoCategoria = new HashMap<>();
                datoCategoria.put("categoria", categoria.getNombre());
                datoCategoria.put("cantidad", cantidad);
                
                datos.add(datoCategoria);
            }
        }
        
        return ResponseEntity.ok(datos);
    }
    @GetMapping("/administracion/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TicketResponseDto>> obtenerTodosTicketsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "fechaCreacion,desc") String sort) {
        
        // Construir el Sort
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sorting = Sort.by(direction, sortParams[0]);
        
        Pageable pageable = PageRequest.of(page, size, sorting);
        
        // Usar el servicio para obtener todos los tickets con filtros
        Page<Ticket> tickets = ticketService.obtenerTodosTicketsConFiltros(
            estado, prioridad, categoriaId, tecnicoId, usuarioId, 
            fechaDesde, fechaHasta, busqueda, pageable);
        
        return ResponseEntity.ok(tickets.map(this::convertirAResponseDto));

    }

    @GetMapping("/administracion/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasAdmin() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Total de tickets
        long total = ticketRepository.count();
        
        // Contar tickets por estado
        long abiertos = ticketRepository.countByEstado(EstadoTicket.NUEVO);
        long asignados = ticketRepository.countByEstado(EstadoTicket.ASIGNADO);
        long resueltos = ticketRepository.countByEstado(EstadoTicket.RESUELTO);
        long cerrados = ticketRepository.countByEstado(EstadoTicket.CERRADO);
        
        estadisticas.put("total", total);
        estadisticas.put("abiertos", abiertos);
        estadisticas.put("asignados", asignados);
        estadisticas.put("resueltos", resueltos);
        estadisticas.put("cerrados", cerrados);
        
        // Estadísticas por prioridad
        Map<String, Long> porPrioridad = new HashMap<>();
        for (PrioridadTicket prioridad : PrioridadTicket.values()) {
            porPrioridad.put(prioridad.name(), ticketRepository.countByPrioridad(prioridad));
        }
        estadisticas.put("porPrioridad", porPrioridad);
        
        // Estadísticas por técnico
        Map<String, Long> porTecnico = new HashMap<>();
        // Implementar lógica para obtener tickets por técnico
        
        estadisticas.put("porTecnico", porTecnico);
        
        return ResponseEntity.ok(estadisticas);
    }

    @PostMapping("/administracion/asignar-masivo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> asignarTicketsMasivo(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            // Obtener la lista de IDs de tickets y convertirla adecuadamente
            List<Object> ticketIdsObj = (List<Object>) payload.get("ticketIds");
            List<Long> ticketIds = ticketIdsObj.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            return Long.parseLong(id.toString());
                        }
                    })
                    .collect(Collectors.toList());
            
            // Convertir el ID del técnico adecuadamente
            Object tecnicoIdObj = payload.get("tecnicoId");
            Long tecnicoId;
            if (tecnicoIdObj instanceof Integer) {
                tecnicoId = ((Integer) tecnicoIdObj).longValue();
            } else if (tecnicoIdObj instanceof Long) {
                tecnicoId = (Long) tecnicoIdObj;
            } else {
                tecnicoId = Long.parseLong(tecnicoIdObj.toString());
            }
            
        String email = authentication.getName();
        
        try {
            ticketService.asignarTicketsMasivo(ticketIds, tecnicoId, email);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            // Devolver un error 403 Forbidden
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
        catch (Exception e) {
            // Manejar cualquier otro error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/administracion/cambiar-estado-masivo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstadoMasivo(@RequestBody Map<String, Object> payload, Authentication authentication) {
        // Convertir IDs de tickets de Integer a Long
        List<Object> ticketIdsObj = (List<Object>) payload.get("ticketIds");
        List<Long> ticketIds = ticketIdsObj.stream()
                .map(id -> {
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else if (id instanceof Long) {
                        return (Long) id;
                    } else {
                        return Long.parseLong(id.toString());
                    }
                })
                .collect(Collectors.toList());
        
        String nuevoEstado = payload.get("nuevoEstado").toString();
        String email = authentication.getName();
        
        EstadoTicket estado = EstadoTicket.valueOf(nuevoEstado);
        ticketService.cambiarEstadoMasivo(ticketIds, estado, email);
        
        return ResponseEntity.ok().build();
    }
    @GetMapping("/ultimo-correlativo")
    public ResponseEntity<Map<String, Integer>> obtenerUltimoCorrelativo() {
        logger.info("GET /api/tickets/ultimo-correlativo - Obteniendo último correlativo");
        
        Integer ultimoNumero = ticketService.obtenerUltimoNumeroCorrelativo();
        
        Map<String, Integer> respuesta = new HashMap<>();
        respuesta.put("ultimoNumero", ultimoNumero);
        
        logger.info("Último número correlativo: {}", ultimoNumero);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/resetear-correlativo")
    public ResponseEntity<Map<String, Boolean>> resetearCorrelativo() {
        logger.info("POST /api/tickets/resetear-correlativo - Reseteando correlativo");
        
        try {
            ticketService.resetearCorrelativo();
            
            Map<String, Boolean> respuesta = new HashMap<>();
            respuesta.put("success", true);
            
            logger.info("Correlativo reseteado correctamente");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            logger.error("Error al resetear correlativo: {}", e.getMessage());
            
            Map<String, Boolean> respuesta = new HashMap<>();
            respuesta.put("success", false);
            
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    @GetMapping("/mis-estadisticas")
public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsuario(Authentication authentication) {
    String email = authentication.getName();
    Map<String, Object> estadisticas = new HashMap<>();
    
    // Obtener tickets del usuario
    List<Ticket> tickets = ticketRepository.findByUsuarioCreadorEmail(email);
    
    // Contar por estado
    long misTickets = tickets.size();
    long pendientes = tickets.stream()
        .filter(t -> t.getEstado() == EstadoTicket.NUEVO || t.getEstado() == EstadoTicket.ASIGNADO)
        .count();
    long enProgreso = tickets.stream()
        .filter(t -> t.getEstado() == EstadoTicket.EN_PROGRESO)
        .count();
    long resueltos = tickets.stream()
        .filter(t -> t.getEstado() == EstadoTicket.RESUELTO)
        .count();
    
    estadisticas.put("misTickets", misTickets);
    estadisticas.put("pendientes", pendientes);
    estadisticas.put("enProgreso", enProgreso);
    estadisticas.put("resueltos", resueltos);
    
    return ResponseEntity.ok(estadisticas);
}

// Datos mensuales para usuarios normales (solo sus propios tickets)
@GetMapping("/mis-datos-mensuales")
public ResponseEntity<List<Map<String, Object>>> obtenerDatosMensualesUsuario(Authentication authentication) {
    String email = authentication.getName();
    List<Map<String, Object>> datos = new ArrayList<>();
    
    // Obtener tickets del usuario
    List<Ticket> tickets = ticketRepository.findByUsuarioCreadorEmail(email);
    
    // Obtener últimos 6 meses
    LocalDate fechaActual = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM").withLocale(new Locale("es", "ES"));
    
    for (int i = 5; i >= 0; i--) {
        LocalDate fechaMes = fechaActual.minusMonths(i);
        String nombreMes = fechaMes.format(formatter).toLowerCase();
        
        // Determinar inicio y fin del mes
        YearMonth yearMonth = YearMonth.from(fechaMes);
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fin = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // Contar tickets creados en ese período
        long cantidad = tickets.stream()
            .filter(t -> t.getFechaCreacion().isAfter(inicio) && t.getFechaCreacion().isBefore(fin))
            .count();
        
        Map<String, Object> datoMes = new HashMap<>();
        datoMes.put("mes", nombreMes);
        datoMes.put("cantidad", cantidad);
        
        datos.add(datoMes);
    }
    
    return ResponseEntity.ok(datos);
}

// Datos por categoría para usuarios normales (solo sus propios tickets)
@GetMapping("/mis-datos-categoria")
public ResponseEntity<List<Map<String, Object>>> obtenerDatosPorCategoriaUsuario(Authentication authentication) {
    String email = authentication.getName();
    List<Map<String, Object>> datos = new ArrayList<>();
    
    // Obtener tickets del usuario
    List<Ticket> tickets = ticketRepository.findByUsuarioCreadorEmail(email);
    
    // Agrupar por categoría
    Map<String, Long> categoriasCount = new HashMap<>();
    
    for (Ticket ticket : tickets) {
        if (ticket.getCategoria() != null) {
            String categoriaNombre = ticket.getCategoria().getNombre();
            Long count = categoriasCount.getOrDefault(categoriaNombre, 0L);
            categoriasCount.put(categoriaNombre, count + 1);
        }
    }
    
    // Convertir a lista de mapas
    for (Map.Entry<String, Long> entry : categoriasCount.entrySet()) {
        Map<String, Object> datoCategoria = new HashMap<>();
        datoCategoria.put("categoria", entry.getKey());
        datoCategoria.put("cantidad", entry.getValue());
        datos.add(datoCategoria);
    }
    
    return ResponseEntity.ok(datos);
}

@GetMapping("/asignados-por-supervisor")
public ResponseEntity<Page<TicketResponseDto>> obtenerTicketsAsignadosPorSupervisor(
        Pageable pageable,
        Authentication authentication) {
    
    String email = authentication.getName();
    Page<Ticket> tickets = ticketService.obtenerTicketsAsignadosPorSupervisor(email, pageable);
    return ResponseEntity.ok(tickets.map(this::convertirAResponseDto));



    }

}