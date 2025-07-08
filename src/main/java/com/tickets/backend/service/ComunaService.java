package com.tickets.backend.service;

import com.tickets.backend.dto.ComunaDto;
import com.tickets.backend.models.Comuna;
import com.tickets.backend.models.Region;
import com.tickets.backend.repository.ComunaRepository;
import com.tickets.backend.repository.RegionRepository;
import jakarta.persistence.EntityNotFoundException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComunaService {
    
    @Autowired
    private ComunaRepository comunaRepository;
    
    @Autowired
    private RegionRepository regionRepository;
    
    // Obtener todas las comunas
    public List<Comuna> obtenerTodasComunas() {
        return comunaRepository.findAll();
    }
    
    // Obtener todas las comunas activas
    public List<Comuna> obtenerComunasActivas() {
        return comunaRepository.findByActivoTrue();
    }
    
    // Obtener todas las comunas de una región
    public List<Comuna> obtenerComunasPorRegion(Long regionId) {
        return comunaRepository.findByRegionId(regionId);
    }
    
    // Obtener todas las comunas activas de una región
    public List<Comuna> obtenerComunasActivasPorRegion(Long regionId) {
        return comunaRepository.findByRegionIdAndActivoTrue(regionId);
    }
    
    // Obtener comuna por ID
    public Comuna obtenerComunaPorId(Long id) {
        return comunaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comuna no encontrada con ID: " + id));
    }
    
    // Crear una nueva comuna
    @Transactional
    public Comuna crearComuna(ComunaDto comunaDto) {
        // Verificar si la región existe
        Region region = regionRepository.findById(comunaDto.getRegionId())
                .orElseThrow(() -> new EntityNotFoundException("Región no encontrada con ID: " + comunaDto.getRegionId()));
        
        // Verificar si ya existe una comuna con el mismo nombre en la misma región
        if (comunaRepository.existsByNombreAndRegionId(comunaDto.getNombre(), comunaDto.getRegionId())) {
            throw new IllegalArgumentException("Ya existe una comuna con el nombre: " + comunaDto.getNombre() + 
                                              " en la región con ID: " + comunaDto.getRegionId());
        }
        
        // Verificar si ya existe una comuna con el mismo código en la misma región
        if (comunaDto.getCodigo() != null && 
            comunaRepository.existsByCodigoAndRegionId(comunaDto.getCodigo(), comunaDto.getRegionId())) {
            throw new IllegalArgumentException("Ya existe una comuna con el código: " + comunaDto.getCodigo() + 
                                              " en la región con ID: " + comunaDto.getRegionId());
        }
        
        Comuna comuna = new Comuna();
        comuna.setNombre(comunaDto.getNombre());
        comuna.setCodigo(comunaDto.getCodigo());
        comuna.setRegion(region);
        comuna.setActivo(comunaDto.isActivo());
        
        return comunaRepository.save(comuna);
    }
    
    // Actualizar una comuna existente
    @Transactional
    public Comuna actualizarComuna(Long id, ComunaDto comunaDto) {
        Comuna comuna = obtenerComunaPorId(id);
        
        // Verificar si la región existe (si se está cambiando)
        if (!comuna.getRegion().getId().equals(comunaDto.getRegionId())) {
            Region region = regionRepository.findById(comunaDto.getRegionId())
                    .orElseThrow(() -> new EntityNotFoundException("Región no encontrada con ID: " + comunaDto.getRegionId()));
            comuna.setRegion(region);
        }
        
        // Verificar si ya existe otra comuna con el mismo nombre en la misma región
        if (!comuna.getNombre().equals(comunaDto.getNombre()) && 
            comunaRepository.existsByNombreAndRegionId(comunaDto.getNombre(), comunaDto.getRegionId())) {
            throw new IllegalArgumentException("Ya existe una comuna con el nombre: " + comunaDto.getNombre() + 
                                              " en la región con ID: " + comunaDto.getRegionId());
        }
        
        // Verificar si ya existe otra comuna con el mismo código en la misma región
        if (comunaDto.getCodigo() != null && 
            !comunaDto.getCodigo().equals(comuna.getCodigo()) && 
            comunaRepository.existsByCodigoAndRegionId(comunaDto.getCodigo(), comunaDto.getRegionId())) {
            throw new IllegalArgumentException("Ya existe una comuna con el código: " + comunaDto.getCodigo() + 
                                              " en la región con ID: " + comunaDto.getRegionId());
        }
        
        comuna.setNombre(comunaDto.getNombre());
        comuna.setCodigo(comunaDto.getCodigo());
        comuna.setActivo(comunaDto.isActivo());
        
        return comunaRepository.save(comuna);
    }
    
    // Eliminar una comuna (desactivar)
    @Transactional
    public void desactivarComuna(Long id) {
        Comuna comuna = obtenerComunaPorId(id);
        comuna.setActivo(false);
        comunaRepository.save(comuna);
    }
    
    // Activar una comuna
    @Transactional
    public void activarComuna(Long id) {
        Comuna comuna = obtenerComunaPorId(id);
        comuna.setActivo(true);
        comunaRepository.save(comuna);
    }

    public Object obtenerPorRegionId(Long regionId) {
     
        throw new UnsupportedOperationException("Unimplemented method 'obtenerPorRegionId'");
    }

    public Object guardar(Comuna comuna) {
        
        throw new UnsupportedOperationException("Unimplemented method 'guardar'");
    }
}