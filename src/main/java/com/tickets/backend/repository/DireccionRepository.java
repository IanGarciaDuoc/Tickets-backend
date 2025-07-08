package com.tickets.backend.repository;

import com.tickets.backend.models.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    
    // Buscar direcciones por comuna
    List<Direccion> findByComunaId(Long comunaId);
    
    // Buscar direcciones activas por comuna
    List<Direccion> findByComunaIdAndActivoTrue(Long comunaId);
    
    // Buscar direcciones por calle (búsqueda parcial)
    List<Direccion> findByCalleContaining(String calle);
    
    // Buscar direcciones por calle y número
    List<Direccion> findByCalleAndNumero(String calle, String numero);
    
    // Buscar direcciones activas
    List<Direccion> findByActivoTrue();
}