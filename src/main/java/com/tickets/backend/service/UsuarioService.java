// src/main/java/com/tickets/backend/service/UsuarioService.java
package com.tickets.backend.service;

import com.tickets.backend.dto.UsuarioDto;
import com.tickets.backend.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface UsuarioService {
    
    Page<Usuario> obtenerTodosUsuarios(Pageable pageable);
    
    List<Usuario> obtenerUsuariosActivos();
 
    List<Usuario> obtenerUsuariosInactivos();

    Usuario obtenerUsuarioPorId(Long id);
    
    Usuario crearUsuario(UsuarioDto usuarioDto);
    
    Usuario actualizarUsuario(Long id, UsuarioDto usuarioDto);
    
    void desactivarUsuario(Long id);
    
    void eliminarUsuario(Long id);
    
    void activarUsuario(Long id);
    
    List<Usuario> obtenerTecnicosActivos();
    
    List<Usuario> obtenerTecnicosPorEspecialidad(String especialidad);

    Usuario asignarRolesAUsuario(Long usuarioId, Set<Long> rolesIds);
    
    List<Usuario> obtenerTecnicosPorCategoria(Long categoriaId);
    
    List<Usuario> obtenerSupervisoresPorCategoria(Long categoriaId);
    
    // NUEVO: Método de búsqueda con filtros
    Page<Usuario> buscarUsuarios(Pageable pageable, Boolean activo, String search, Long empresaId, Long rolId);
    // Agregar este método a la interfaz UsuarioService.java
void cambiarPassword(Long usuarioId, String currentPassword, String newPassword);
}