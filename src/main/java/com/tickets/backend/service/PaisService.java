package com.tickets.backend.service;

import com.tickets.backend.dto.PaisDto;
import com.tickets.backend.models.Pais;
import com.tickets.backend.repository.PaisRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaisService {
    
    @Autowired
    private PaisRepository paisRepository;
    
    // Obtener todos los países
    public List<Pais> obtenerTodosPaises() {
        return paisRepository.findAll();
    }
    
    // Obtener todos los países activos
    public List<Pais> obtenerPaisesActivos() {
        return paisRepository.findByActivoTrue();
    }
    
    // Obtener país por ID
    public Pais obtenerPaisPorId(Long id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("País no encontrado con ID: " + id));
    }
    
    // Crear un nuevo país
    @Transactional
    public Pais crearPais(PaisDto paisDto) {
        // Verificar si ya existe un país con el mismo nombre
        if (paisRepository.existsByNombre(paisDto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un país con el nombre: " + paisDto.getNombre());
        }
        
        // Verificar si ya existe un país con el mismo código
        if (paisDto.getCodigo() != null && paisRepository.existsByCodigo(paisDto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un país con el código: " + paisDto.getCodigo());
        }
        
        Pais pais = new Pais();
        pais.setNombre(paisDto.getNombre());
        pais.setCodigo(paisDto.getCodigo());
        pais.setActivo(paisDto.isActivo());
        
        return paisRepository.save(pais);
    }
    
    // Actualizar un país existente
    @Transactional
    public Pais actualizarPais(Long id, PaisDto paisDto) {
        Pais pais = obtenerPaisPorId(id);
        
        // Verificar si ya existe otro país con el mismo nombre
        if (!pais.getNombre().equals(paisDto.getNombre()) && 
            paisRepository.existsByNombre(paisDto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un país con el nombre: " + paisDto.getNombre());
        }
        
        // Verificar si ya existe otro país con el mismo código
        if (paisDto.getCodigo() != null && 
            !paisDto.getCodigo().equals(pais.getCodigo()) && 
            paisRepository.existsByCodigo(paisDto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un país con el código: " + paisDto.getCodigo());
        }
        
        pais.setNombre(paisDto.getNombre());
        pais.setCodigo(paisDto.getCodigo());
        pais.setActivo(paisDto.isActivo());
        
        return paisRepository.save(pais);
    }
    
    // Eliminar un país (desactivar)
    @Transactional
    public void desactivarPais(Long id) {
        Pais pais = obtenerPaisPorId(id);
        pais.setActivo(false);
        paisRepository.save(pais);
    }
    
    // Activar un país
    @Transactional
    public void activarPais(Long id) {
        Pais pais = obtenerPaisPorId(id);
        pais.setActivo(true);
        paisRepository.save(pais);
    }
}