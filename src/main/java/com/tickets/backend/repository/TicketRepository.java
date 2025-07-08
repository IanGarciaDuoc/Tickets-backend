package com.tickets.backend.repository;

import com.tickets.backend.models.Categoria;
import com.tickets.backend.models.EstadoTicket;
import com.tickets.backend.models.PrioridadTicket;
import com.tickets.backend.models.Ticket;
import com.tickets.backend.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUsuarioCreador_Id(Long id);
    
    Page<Ticket> findByEstado(EstadoTicket estado, Pageable pageable);
    
    Page<Ticket> findByPrioridad(PrioridadTicket prioridad, Pageable pageable);
    
    Page<Ticket> findByUsuarioCreador(Usuario usuarioCreador, Pageable pageable);
    
    Page<Ticket> findByTecnicoAsignado(Usuario tecnicoAsignado, Pageable pageable);
    
    Page<Ticket> findByTecnicoAsignadoIsNull(Pageable pageable);
    
    List<Ticket> findByCategoriaId(Long categoriaId);
    
    List<Ticket> findBySubcategoriaId(Long subcategoriaId);

    Optional<Ticket> findTopByOrderByIdDesc();
    
    @Query("SELECT t FROM Ticket t WHERE t.estado = :estado AND t.tecnicoAsignado.especialidad = :especialidad")
    List<Ticket> findByEstadoAndEspecialidad(@Param("estado") EstadoTicket estado, @Param("especialidad") String especialidad);
    
    @Query("SELECT t FROM Ticket t WHERE LOWER(CAST(t.titulo AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(CAST(t.descripcion AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Ticket> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.estado = :estado")
    Long countByEstado(@Param("estado") EstadoTicket estado);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.prioridad = :prioridad")
    Long countByPrioridad(@Param("prioridad") PrioridadTicket prioridad);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.fechaCreacion >= :startDate AND t.fechaCreacion <= :endDate")
    Long countByFechaCreacionBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Ticket t JOIN t.etiquetas e WHERE e.id = :etiquetaId")
    Page<Ticket> findByEtiquetaId(@Param("etiquetaId") Long etiquetaId, Pageable pageable);

    // Contar tickets por prioridad (múltiples prioridades)
    long countByPrioridadIn(List<PrioridadTicket> prioridades);
    
    // Contar tickets por categoría
    long countByCategoria(Categoria categoria);
    
    // Método nuevo para buscar tickets resueltos con fecha de resolución anterior a una fecha límite
    List<Ticket> findByEstadoAndFechaResolucionLessThan(EstadoTicket estado, LocalDateTime fechaLimite);
    
    // Opcional: Si quieres una consulta más avanzada para los datos mensuales
    @Query(value = "SELECT MONTH(fecha_creacion) as mes, COUNT(*) as cantidad " +
                   "FROM tickets " +
                   "WHERE fecha_creacion >= :fechaInicio " +
                   "GROUP BY MONTH(fecha_creacion) " +
                   "ORDER BY MONTH(fecha_creacion)", nativeQuery = true)
    List<Object[]> contarTicketsPorMes(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Opcional: Si quieres una consulta más avanzada para datos por categoría
    @Query(value = "SELECT c.nombre as categoria, COUNT(t.id) as cantidad " +
                   "FROM tickets t " +
                   "JOIN categorias c ON t.categoria_id = c.id " +
                   "GROUP BY c.nombre " +
                   "ORDER BY COUNT(t.id) DESC", nativeQuery = true)
    List<Object[]> contarTicketsPorCategoria();

    List<Ticket> findByTecnicoAsignado_Id(Long tecnicoId);



// En TicketRepository.java - Cambiar los parámetros de fechaDesde y fechaHasta
@Query(value = "SELECT * FROM tickets t WHERE " +
       "(:estado IS NULL OR t.estado = :estado) AND " +
       "(:prioridad IS NULL OR t.prioridad = :prioridad) AND " +
       "(:categoriaId IS NULL OR t.categoria_id = :categoriaId) AND " +
       "(:tecnicoId IS NULL OR t.tecnico_asignado_id = :tecnicoId) AND " +
       "(:usuarioId IS NULL OR t.usuario_creador_id = :usuarioId) AND " +
       "(:fechaDesde IS NULL OR t.fecha_creacion >= :fechaDesde) AND " +
       "(:fechaHasta IS NULL OR t.fecha_creacion <= :fechaHasta) AND " +
       "(:busqueda IS NULL OR " +
       "LOWER(CAST(t.titulo AS VARCHAR(MAX))) LIKE LOWER('%' + :busqueda + '%') OR " +
       "LOWER(CAST(t.descripcion AS VARCHAR(MAX))) LIKE LOWER('%' + :busqueda + '%') OR " +
       "LOWER(t.numero_ticket) LIKE LOWER('%' + :busqueda + '%'))",  // Sin ORDER BY aquí
       nativeQuery = true,
       countQuery = "SELECT COUNT(*) FROM tickets t WHERE " +
       "(:estado IS NULL OR t.estado = :estado) AND " +
       "(:prioridad IS NULL OR t.prioridad = :prioridad) AND " +
       "(:categoriaId IS NULL OR t.categoria_id = :categoriaId) AND " +
       "(:tecnicoId IS NULL OR t.tecnico_asignado_id = :tecnicoId) AND " +
       "(:usuarioId IS NULL OR t.usuario_creador_id = :usuarioId) AND " +
       "(:fechaDesde IS NULL OR t.fecha_creacion >= :fechaDesde) AND " +
       "(:fechaHasta IS NULL OR t.fecha_creacion <= :fechaHasta) AND " +
       "(:busqueda IS NULL OR " +
       "LOWER(CAST(t.titulo AS VARCHAR(MAX))) LIKE LOWER('%' + :busqueda + '%') OR " +
       "LOWER(CAST(t.descripcion AS VARCHAR(MAX))) LIKE LOWER('%' + :busqueda + '%') OR " +
       "LOWER(t.numero_ticket) LIKE LOWER('%' + :busqueda + '%'))")
Page<Ticket> buscarConFiltros(
    @Param("estado") String estado,
    @Param("prioridad") String prioridad,
    @Param("categoriaId") Long categoriaId,
    @Param("tecnicoId") Long tecnicoId,
    @Param("usuarioId") Long usuarioId,
    @Param("fechaDesde") LocalDateTime fechaDesde,  // CAMBIAR DE String A LocalDateTime
    @Param("fechaHasta") LocalDateTime fechaHasta,  // CAMBIAR DE String A LocalDateTime
    @Param("busqueda") String busqueda,
    Pageable pageable
);
List<Ticket> findByUsuarioCreadorEmail(String email);
Long countByCategoriaAndEstado(Categoria categoria, EstadoTicket estado);
Long countByTecnicoAsignado(Usuario tecnico);
Long countByTecnicoAsignadoAndEstado(Usuario tecnico, EstadoTicket estado);
@Query("SELECT t FROM Ticket t WHERE t.estado = :estado AND t.fechaResolucion < :fechaLimite")
List<Ticket> findTicketsResueltosParaCierre(
    @Param("estado") EstadoTicket estado,
    @Param("fechaLimite") LocalDateTime fechaLimite
);

// Método alternativo más específico si quieres usar solo tickets resueltos
default List<Ticket> findTicketsResueltosParaCierre(LocalDateTime fechaLimite) {
    return findByEstadoAndFechaResolucionLessThan(EstadoTicket.RESUELTO, fechaLimite);
}
// Método para obtener tickets sin asignar por categoría específica
@Query("SELECT t FROM Ticket t WHERE t.tecnicoAsignado IS NULL AND t.estado = 'NUEVO' AND t.categoria.id = :categoriaId ORDER BY t.prioridad DESC, t.fechaCreacion ASC")
Page<Ticket> findTicketsSinAsignarPorCategoria(@Param("categoriaId") Long categoriaId, Pageable pageable);

// Método para obtener tickets asignados a un técnico con estados específicos
@Query("SELECT t FROM Ticket t WHERE t.tecnicoAsignado = :tecnico AND t.estado IN :estados ORDER BY t.prioridad DESC, t.fechaCreacion ASC")
Page<Ticket> findByTecnicoAsignadoAndEstadoIn(@Param("tecnico") Usuario tecnico, @Param("estados") List<EstadoTicket> estados, Pageable pageable);

// Método para obtener tickets asignados por supervisores específicos (para futura implementación)
@Query("SELECT t FROM Ticket t WHERE t.tecnicoAsignado = :tecnico AND t.estado IN :estados AND EXISTS " +
       "(SELECT st FROM SupervisorTecnico st WHERE st.tecnico = :tecnico AND st.activo = true) " +
       "ORDER BY t.prioridad DESC, t.fechaCreacion ASC")
Page<Ticket> findTicketsAsignadosPorSupervisor(@Param("tecnico") Usuario tecnico, @Param("estados") List<EstadoTicket> estados, Pageable pageable);

// Método para contar tickets sin asignar por categoría
@Query("SELECT COUNT(t) FROM Ticket t WHERE t.tecnicoAsignado IS NULL AND t.estado = 'NUEVO' AND t.categoria.id = :categoriaId")
Long countTicketsSinAsignarPorCategoria(@Param("categoriaId") Long categoriaId);

}
