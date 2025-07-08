package com.tickets.backend.service.impl;

import com.tickets.backend.exceptions.ResourceNotFoundException;
import com.tickets.backend.models.Permiso;
import com.tickets.backend.repository.PermisoRepository;
import com.tickets.backend.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermisoServiceImpl implements PermisoService {

    @Autowired
    private PermisoRepository permisoRepository;

    @Override
    public List<Permiso> obtenerTodosPermisos() {
        return permisoRepository.findAll();
    }
    
    @Override
    public List<Permiso> obtenerPermisosPorModulo(String modulo) {
        return permisoRepository.findByModulo(modulo);
    }

    @Override
    public Permiso obtenerPermisoPorId(Long id) {
        return permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Permiso crearPermiso(Permiso permiso) {
        // Verificar si ya existe un permiso con ese nombre
        if(permisoRepository.findByNombre(permiso.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un permiso con el nombre: " + permiso.getNombre());
        }
        return permisoRepository.save(permiso);
    }

    @Override
    @Transactional
    public Permiso actualizarPermiso(Long id, Permiso permisoRequest) {
        Permiso permiso = obtenerPermisoPorId(id);
        
        // Verificar si el nombre existe pero pertenece a otro permiso
        if(!permiso.getNombre().equals(permisoRequest.getNombre()) 
                && permisoRepository.findByNombre(permisoRequest.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un permiso con el nombre: " + permisoRequest.getNombre());
        }
        
        permiso.setNombre(permisoRequest.getNombre());
        permiso.setDescripcion(permisoRequest.getDescripcion());
        permiso.setModulo(permisoRequest.getModulo());
        permiso.setAccion(permisoRequest.getAccion());
        
        return permisoRepository.save(permiso);
    }

    @Override
    @Transactional
    public void eliminarPermiso(Long id) {
        if(!permisoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permiso no encontrado con ID: " + id);
        }
        permisoRepository.deleteById(id);
    }
}