
package com.tickets.backend.repository;

import com.tickets.backend.models.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {
    
    List<Adjunto> findByTicketId(Long ticketId);
    
    List<Adjunto> findByUsuarioId(Long usuarioId);
    
    void deleteByTicketId(Long ticketId);
    
    List<Adjunto> findByTipoArchivo(String tipoArchivo);
}