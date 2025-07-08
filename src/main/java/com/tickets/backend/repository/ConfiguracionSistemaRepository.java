package com.tickets.backend.repository;

import com.tickets.backend.models.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para manejar operaciones de base de datos con la entidad ConfiguracionSistema.
 */
@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {
    
    /**
     * Busca una configuración por su clave.
     * 
     * @param clave La clave de la configuración a buscar
     * @return Un Optional con la configuración si existe, o vacío si no
     */
    Optional<ConfiguracionSistema> findByClave(String clave);
    
    /**
     * Verifica si existe una configuración con la clave dada.
     * 
     * @param clave La clave a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByClave(String clave);
    
    /**
     * Elimina una configuración por su clave.
     * 
     * @param clave La clave de la configuración a eliminar
     */
    void deleteByClave(String clave);
  
    /**
     * Buscar configuracion por clave
     */
   
    
    /**
     * Buscar configuraciones por tipo
     */
    List<ConfiguracionSistema> findByTipo(String tipo);
    
    /**
     * Buscar configuraciones por lista de claves
     */
    List<ConfiguracionSistema> findByClaveIn(List<String> claves);
@Query("SELECT c FROM ConfiguracionSistema c WHERE c.clave = :clave")
    Optional<ConfiguracionSistema> findByClaveWithQuery(@Param("clave") String clave);
}

