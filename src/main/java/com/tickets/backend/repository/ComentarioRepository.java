package com.tickets.backend.repository;

import com.tickets.backend.models.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    
    List<Comentario> findByTicketId(Long ticketId);
    
    List<Comentario> findByTicketIdOrderByFechaCreacionDesc(Long ticketId);
    
    List<Comentario> findByUsuarioId(Long usuarioId);
    
    List<Comentario> findByTicketIdAndEsPrivadoFalse(Long ticketId);
    
    void deleteByTicketId(Long ticketId);
}