package com.tickets.backend.repository;

import com.tickets.backend.models.SupervisorTecnico;
import com.tickets.backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorTecnicoRepository extends JpaRepository<SupervisorTecnico, Long> {
    
    // Encontrar técnicos de un supervisor
    @Query("SELECT st FROM SupervisorTecnico st WHERE st.supervisor.id = :supervisorId AND st.activo = true")
    List<SupervisorTecnico> findTecnicosBySupervisor(@Param("supervisorId") Long supervisorId);
    
    // Encontrar supervisores de un técnico
    @Query("SELECT st FROM SupervisorTecnico st WHERE st.tecnico.id = :tecnicoId AND st.activo = true")
    List<SupervisorTecnico> findSupervisoresByTecnico(@Param("tecnicoId") Long tecnicoId);
    
    // Encontrar una relación específica
    Optional<SupervisorTecnico> findBySupervisorAndTecnicoAndActivoTrue(Usuario supervisor, Usuario tecnico);
    
    // Obtener todos los técnicos de un supervisor (solo los objetos Usuario)
    @Query("SELECT st.tecnico FROM SupervisorTecnico st WHERE st.supervisor.id = :supervisorId AND st.activo = true")
    List<Usuario> findTecnicosUsuariosBySupervisor(@Param("supervisorId") Long supervisorId);
    
    // Verificar si un usuario supervisa a otro
    @Query("SELECT COUNT(st) > 0 FROM SupervisorTecnico st WHERE st.supervisor.id = :supervisorId AND st.tecnico.id = :tecnicoId AND st.activo = true")
    boolean existsSupervisorTecnico(@Param("supervisorId") Long supervisorId, @Param("tecnicoId") Long tecnicoId);
    
    // Obtener técnicos de la misma categoría que un supervisor
    @Query("SELECT st.tecnico FROM SupervisorTecnico st " +
           "WHERE st.supervisor.id = :supervisorId " +
           "AND st.activo = true " +
           "AND st.tecnico.categoria.id = :categoriaId")
    List<Usuario> findTecnicosBySupervisorAndCategoria(@Param("supervisorId") Long supervisorId, 
                                                       @Param("categoriaId") Long categoriaId);
}