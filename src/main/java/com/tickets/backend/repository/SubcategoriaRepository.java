package com.tickets.backend.repository;

import com.tickets.backend.models.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {
    
    List<Subcategoria> findByCategoriaId(Long categoriaId);
    
    List<Subcategoria> findByCategoriaIdAndActivoTrue(Long categoriaId);
    
    Optional<Subcategoria> findByNombreAndCategoriaId(String nombre, Long categoriaId);
    
    boolean existsByNombreAndCategoriaId(String nombre, Long categoriaId);
}