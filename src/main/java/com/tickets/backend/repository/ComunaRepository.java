package com.tickets.backend.repository;

import com.tickets.backend.models.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    
    // Buscar comuna por nombre
    Optional<Comuna> findByNombre(String nombre);
    
    // Buscar comuna por código
    Optional<Comuna> findByCodigo(String codigo);
    
    // Listar todas las comunas de una región
    List<Comuna> findByRegionId(Long regionId);
    
    // Listar todas las comunas activas de una región
    List<Comuna> findByRegionIdAndActivoTrue(Long regionId);
    
    // Listar todas las comunas activas
    List<Comuna> findByActivoTrue();
    
    // Verificar si existe una comuna con el mismo nombre en la misma región
    boolean existsByNombreAndRegionId(String nombre, Long regionId);
    
    // Verificar si existe una comuna con el mismo código en la misma región
    boolean existsByCodigoAndRegionId(String codigo, Long regionId);
}