package com.tickets.backend.repository;

import com.tickets.backend.models.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    
    // Buscar región por nombre
    Optional<Region> findByNombre(String nombre);
    
    // Buscar región por código
    Optional<Region> findByCodigo(String codigo);
    
    // Listar todas las regiones de un país
    List<Region> findByPaisId(Long paisId);
    
    // Listar todas las regiones activas de un país
    List<Region> findByPaisIdAndActivoTrue(Long paisId);
    
    // Listar todas las regiones activas
    List<Region> findByActivoTrue();
    
    // Verificar si existe una región con el mismo nombre en el mismo país
    boolean existsByNombreAndPaisId(String nombre, Long paisId);
    
    // Verificar si existe una región con el mismo código en el mismo país
    boolean existsByCodigoAndPaisId(String codigo, Long paisId);
}