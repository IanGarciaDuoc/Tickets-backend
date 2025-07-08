// NotificacionCategoriaRepository.java
package com.tickets.backend.repository;

import com.tickets.backend.models.NotificacionCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionCategoriaRepository extends JpaRepository<NotificacionCategoria, Long> {
    List<NotificacionCategoria> findByCategoriaIdAndActivoTrue(Long categoriaId);
}