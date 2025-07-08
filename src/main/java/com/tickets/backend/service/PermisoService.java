package com.tickets.backend.service;

import com.tickets.backend.models.Permiso;
import java.util.List;

public interface PermisoService {
    List<Permiso> obtenerTodosPermisos();
    
    List<Permiso> obtenerPermisosPorModulo(String modulo);
    
    Permiso obtenerPermisoPorId(Long id);
    
    Permiso crearPermiso(Permiso permiso);
    
    Permiso actualizarPermiso(Long id, Permiso permiso);
    
    void eliminarPermiso(Long id);
}