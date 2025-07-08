package com.tickets.backend.service;

import com.tickets.backend.models.Permiso;
import com.tickets.backend.models.Rol;
import java.util.List;
import java.util.Set;

public interface RolService {
    List<Rol> obtenerTodosRoles();
    
    Rol obtenerRolPorId(Long id);
    
    Rol crearRol(Rol rol);
    
    Rol actualizarRol(Long id, Rol rol);
    
    void eliminarRol(Long id);
    
    Rol asignarPermisosARol(Long rolId, Set<Long> permisosIds);

    Set<Permiso> obtenerPermisosPorRolId(Long rolId);

// Método para verificar si un rol tiene un permiso específico
boolean rolTienePermiso(Long rolId, String nombrePermiso);

// Método para agregar un permiso específico a un rol
Rol agregarPermisoARol(Long rolId, Long permisoId);

// Método para eliminar un permiso específico de un rol
Rol eliminarPermisoDeRol(Long rolId, Long permisoId);
    
}