package com.tickets.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tickets.backend.models.ConfiguracionCorreo;

@Repository
public interface ConfiguracionCorreoRepository extends JpaRepository<ConfiguracionCorreo, Long> {
    
    /**
     * Obtiene la configuración de correo activa
     * @return Configuración activa
     */
    @Query("SELECT c FROM ConfiguracionCorreo c WHERE c.activo = true ORDER BY c.id DESC")
    Optional<ConfiguracionCorreo> findConfiguracionActiva();
    
}