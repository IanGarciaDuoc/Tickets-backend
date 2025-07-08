package com.tickets.backend.repository;

import com.tickets.backend.models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    
    List<Sucursal> findByEmpresaId(Long empresaId);
    
    List<Sucursal> findByEmpresaIdAndActivoTrue(Long empresaId);
    
    Optional<Sucursal> findByNombreAndEmpresaId(String nombre, Long empresaId);
    
    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);
}