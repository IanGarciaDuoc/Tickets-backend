package com.tickets.backend.repository;

import com.tickets.backend.models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    Optional<Empresa> findByNombre(String nombre);
    
    List<Empresa> findByActivoTrue();
    
    boolean existsByNombre(String nombre);
    
    boolean existsByRut(String rut);
}