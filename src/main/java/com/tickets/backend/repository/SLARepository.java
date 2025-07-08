package com.tickets.backend.repository;

import com.tickets.backend.models.PrioridadTicket;
import com.tickets.backend.models.SLA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SLARepository extends JpaRepository<SLA, Long> {
    
    Optional<SLA> findByNombre(String nombre);
    
    Optional<SLA> findByPrioridadAndActivoTrue(PrioridadTicket prioridad);
    
    List<SLA> findByActivoTrue();
    
    boolean existsByNombre(String nombre);
}