// src/main/java/com/tickets/backend/repository/UsuarioRepository.java
package com.tickets.backend.repository;

import com.tickets.backend.models.Empresa;
import com.tickets.backend.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Usuario> findByEspecialidad(String especialidad);
    
    // Este método debe recibir un objeto Empresa en lugar de un String
    List<Usuario> findByEmpresa(Empresa empresa);
    
    // Opcionalmente, puedes agregar esto para buscar por el ID de empresa
    List<Usuario> findByEmpresaId(Long empresaId);
    
    // O esto para buscar por el nombre de la empresa
    List<Usuario> findByEmpresaNombre(String nombreEmpresa);
    
    List<Usuario> findByActivoTrue();

    List<Usuario> findByRolesNombreAndActivoTrue(String rolNombre);
    
    List<Usuario> findByRolesNombreAndEspecialidadAndActivoTrue(String rolNombre, String especialidad);
    
    // NUEVOS MÉTODOS
    List<Usuario> findByRolesNombreAndCategoriaIdAndActivoTrue(String rolNombre, Long categoriaId);
    
    // Para obtener todos los usuarios de una categoría específica
    List<Usuario> findByCategoriaIdAndActivoTrue(Long categoriaId);

    List<Usuario> findByActivoFalse();

    // OPTIMIZADO: Paginación con fetch joins para evitar N+1
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE u.activo = true")
    Page<Usuario> findByActivoTrueOptimized(Pageable pageable);
    
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE u.activo = false")
    Page<Usuario> findByActivoFalseOptimized(Pageable pageable);

    // CORREGIDO: Método principal con fetch joins para SQL Server
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE (:activo IS NULL OR u.activo = :activo) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     (u.especialidad IS NOT NULL AND LOWER(u.especialidad) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "     (e.nombre IS NOT NULL AND LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%')))) " +
           "AND (:empresaId IS NULL OR e.id = :empresaId) " +
           "AND (:rolId IS NULL OR r.id = :rolId)")
    Page<Usuario> buscarUsuariosOptimizado(@Param("activo") Boolean activo,
                                          @Param("search") String search,
                                          @Param("empresaId") Long empresaId,
                                          @Param("rolId") Long rolId,
                                          Pageable pageable);

    // CORREGIDO: Método simple con fetch joins para SQL Server
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE (:activo IS NULL OR u.activo = :activo) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     (e.nombre IS NOT NULL AND LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Usuario> buscarUsuariosSimpleOptimizado(@Param("activo") Boolean activo,
                                                @Param("search") String search,
                                                Pageable pageable);

    // OPTIMIZADO: Para obtener todos los usuarios con fetch joins
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r")
    Page<Usuario> findAllOptimized(Pageable pageable);

    // OPTIMIZADO: Para obtener usuario por ID con todas las relaciones
    @Query("SELECT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE u.id = :id")
    Optional<Usuario> findByIdOptimized(@Param("id") Long id);

    // MÉTODOS DEPRECADOS - mantener para compatibilidad pero marcar como deprecados
    @Deprecated
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN u.empresa e " +
           "LEFT JOIN u.roles r " +
           "WHERE (:activo IS NULL OR u.activo = :activo) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.especialidad) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:empresaId IS NULL OR e.id = :empresaId) " +
           "AND (:rolId IS NULL OR r.id = :rolId)")
    Page<Usuario> buscarUsuarios(@Param("activo") Boolean activo,
                                @Param("search") String search,
                                @Param("empresaId") Long empresaId,
                                @Param("rolId") Long rolId,
                                Pageable pageable);

    @Deprecated
    @Query("SELECT u FROM Usuario u " +
           "LEFT JOIN u.empresa e " +
           "WHERE (:activo IS NULL OR u.activo = :activo) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Usuario> buscarUsuariosSimple(@Param("activo") Boolean activo,
                                      @Param("search") String search,
                                      Pageable pageable);

    // OPTIMIZADO: Métodos específicos con fetch joins
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE r.nombre = :rolNombre AND u.activo = true")
    List<Usuario> findByRolesNombreAndActivoTrueOptimized(@Param("rolNombre") String rolNombre);

    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.empresa e " +
           "LEFT JOIN FETCH u.sucursal s " +
           "LEFT JOIN FETCH u.categoria c " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE r.nombre = :rolNombre AND u.categoria.id = :categoriaId AND u.activo = true")
    List<Usuario> findByRolesNombreAndCategoriaIdAndActivoTrueOptimized(@Param("rolNombre") String rolNombre, 
                                                                       @Param("categoriaId") Long categoriaId);
}