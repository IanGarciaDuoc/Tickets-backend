package com.tickets.backend.repository;

import com.tickets.backend.models.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaisRepository extends JpaRepository<Pais, Long> {
    
    // Buscar país por nombre
    Optional<Pais> findByNombre(String nombre);
    
    // Buscar país por código
    Optional<Pais> findByCodigo(String codigo);
    
    // Listar todos los países activos
    List<Pais> findByActivoTrue();
    
    // Verificar si existe un país con el mismo nombre (para validaciones)
    boolean existsByNombre(String nombre);
    
    // Verificar si existe un país con el mismo código (para validaciones)
    boolean existsByCodigo(String codigo);
}