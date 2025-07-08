package com.tickets.backend.repository;

import com.tickets.backend.models.HistorialCambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialCambioRepository extends JpaRepository<HistorialCambio, Long> {
    
    List<HistorialCambio> findByTicketId(Long ticketId);
    
    List<HistorialCambio> findByTicketIdOrderByFechaCambioDesc(Long ticketId);
    
    List<HistorialCambio> findByUsuarioId(Long usuarioId);
    
    List<HistorialCambio> findByCampoModificado(String campoModificado);
    
    List<HistorialCambio> findByFechaCambioBetween(LocalDateTime inicio, LocalDateTime fin);
    
    void deleteByTicketId(Long ticketId);
}