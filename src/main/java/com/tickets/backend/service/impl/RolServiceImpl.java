package com.tickets.backend.service.impl;

import com.tickets.backend.exceptions.ResourceNotFoundException;
import com.tickets.backend.models.Permiso;
import com.tickets.backend.models.Rol;
import com.tickets.backend.repository.PermisoRepository;
import com.tickets.backend.repository.RolRepository;
import com.tickets.backend.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RolServiceImpl implements RolService {

    @Override
    @Transactional
    public Rol agregarPermisoARol(Long rolId, Long permisoId) {
        Rol rol = obtenerRolPorId(rolId);
        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + permisoId));
        rol.getPermisos().add(permiso);
        return rolRepository.save(rol);
    }

    @Override
    public Set<Permiso> obtenerPermisosPorRolId(Long rolId) {
        Rol rol = obtenerRolPorId(rolId);
        return rol.getPermisos();
    }

    @Override
    @Transactional
    public Rol eliminarPermisoDeRol(Long rolId, Long permisoId) {
        Rol rol = obtenerRolPorId(rolId);
        Permiso permiso = permisoRepository.findById(permisoId)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + permisoId));
        rol.getPermisos().remove(permiso);
        return rolRepository.save(rol);
    }

    @Override
    public boolean rolTienePermiso(Long rolId, String permisoNombre) {
        Rol rol = obtenerRolPorId(rolId);
        return rol.getPermisos().stream()
                .anyMatch(permiso -> permiso.getNombre().equals(permisoNombre));
    }

    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PermisoRepository permisoRepository;

    @Override
    public List<Rol> obtenerTodosRoles() {
        return rolRepository.findAll();
    }

    @Override
    public Rol obtenerRolPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Rol crearRol(Rol rol) {
        // Verificar si ya existe un rol con ese nombre
        if(rolRepository.findByNombre(rol.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + rol.getNombre());
        }
        return rolRepository.save(rol);
    }

    @Override
    @Transactional
    public Rol actualizarRol(Long id, Rol rolRequest) {
        Rol rol = obtenerRolPorId(id);
        
        // Verificar si el nombre existe pero pertenece a otro rol
        if(!rol.getNombre().equals(rolRequest.getNombre()) 
                && rolRepository.findByNombre(rolRequest.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + rolRequest.getNombre());
        }
        
        rol.setNombre(rolRequest.getNombre());
        rol.setDescripcion(rolRequest.getDescripcion());
        
        return rolRepository.save(rol);
    }

    @Override
    @Transactional
    public void eliminarRol(Long id) {
        if(!rolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
        rolRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Rol asignarPermisosARol(Long rolId, Set<Long> permisosIds) {
        Rol rol = obtenerRolPorId(rolId);
        
        Set<Permiso> permisos = new HashSet<>();
        for(Long permisoId : permisosIds) {
            Permiso permiso = permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + permisoId));
            permisos.add(permiso);
        }
        
        rol.setPermisos(permisos);
        return rolRepository.save(rol);
    }
}