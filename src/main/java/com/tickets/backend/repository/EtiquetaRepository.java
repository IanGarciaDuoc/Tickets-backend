package com.tickets.backend.repository;

import com.tickets.backend.models.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {
    
    Optional<Etiqueta> findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);
    
    @Query("SELECT e FROM Etiqueta e JOIN e.tickets t WHERE t.id = :ticketId")
    List<Etiqueta> findByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT e FROM Etiqueta e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Etiqueta> searchByKeyword(@Param("keyword") String keyword);
}