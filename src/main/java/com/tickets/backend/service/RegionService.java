package com.tickets.backend.service;

import com.tickets.backend.dto.RegionDto;
import com.tickets.backend.models.Pais;
import com.tickets.backend.models.Region;
import com.tickets.backend.repository.PaisRepository;
import com.tickets.backend.repository.RegionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegionService {
    
    @Autowired
    private RegionRepository regionRepository;
    
    @Autowired
    private PaisRepository paisRepository;
    
    // Obtener todas las regiones
    public List<Region> obtenerTodasRegiones() {
        return regionRepository.findAll();
    }
    
    // Obtener todas las regiones activas
    public List<Region> obtenerRegionesActivas() {
        return regionRepository.findByActivoTrue();
    }
    
    // Obtener todas las regiones de un país
    public List<Region> obtenerRegionesPorPais(Long paisId) {
        return regionRepository.findByPaisId(paisId);
    }
    
    // Obtener todas las regiones activas de un país
    public List<Region> obtenerRegionesActivasPorPais(Long paisId) {
        return regionRepository.findByPaisIdAndActivoTrue(paisId);
    }
    
    // Obtener región por ID
    public Region obtenerRegionPorId(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Región no encontrada con ID: " + id));
    }
    
    // Crear una nueva región
    @Transactional
    public Region crearRegion(RegionDto regionDto) {
        // Verificar si el país existe
        Pais pais = paisRepository.findById(regionDto.getPaisId())
                .orElseThrow(() -> new EntityNotFoundException("País no encontrado con ID: " + regionDto.getPaisId()));
        
        // Verificar si ya existe una región con el mismo nombre en el mismo país
        if (regionRepository.existsByNombreAndPaisId(regionDto.getNombre(), regionDto.getPaisId())) {
            throw new IllegalArgumentException("Ya existe una región con el nombre: " + regionDto.getNombre() + 
                                              " en el país con ID: " + regionDto.getPaisId());
        }
        
        // Verificar si ya existe una región con el mismo código en el mismo país
        if (regionDto.getCodigo() != null && 
            regionRepository.existsByCodigoAndPaisId(regionDto.getCodigo(), regionDto.getPaisId())) {
            throw new IllegalArgumentException("Ya existe una región con el código: " + regionDto.getCodigo() + 
                                              " en el país con ID: " + regionDto.getPaisId());
        }
        
        Region region = new Region();
        region.setNombre(regionDto.getNombre());
        region.setCodigo(regionDto.getCodigo());
        region.setPais(pais);
        region.setActivo(regionDto.isActivo());
        
        return regionRepository.save(region);
    }
    
    // Actualizar una región existente
    @Transactional
    public Region actualizarRegion(Long id, RegionDto regionDto) {
        Region region = obtenerRegionPorId(id);
        
        // Verificar si el país existe (si se está cambiando)
        if (!region.getPais().getId().equals(regionDto.getPaisId())) {
            Pais pais = paisRepository.findById(regionDto.getPaisId())
                    .orElseThrow(() -> new EntityNotFoundException("País no encontrado con ID: " + regionDto.getPaisId()));
            region.setPais(pais);
        }
        
        // Verificar si ya existe otra región con el mismo nombre en el mismo país
        if (!region.getNombre().equals(regionDto.getNombre()) && 
            regionRepository.existsByNombreAndPaisId(regionDto.getNombre(), regionDto.getPaisId())) {
            throw new IllegalArgumentException("Ya existe una región con el nombre: " + regionDto.getNombre() + 
                                              " en el país con ID: " + regionDto.getPaisId());
        }
        
        // Verificar si ya existe otra región con el mismo código en el mismo país
        if (regionDto.getCodigo() != null && 
            !regionDto.getCodigo().equals(region.getCodigo()) && 
            regionRepository.existsByCodigoAndPaisId(regionDto.getCodigo(), regionDto.getPaisId())) {
            throw new IllegalArgumentException("Ya existe una región con el código: " + regionDto.getCodigo() + 
                                              " en el país con ID: " + regionDto.getPaisId());
        }
        
        region.setNombre(regionDto.getNombre());
        region.setCodigo(regionDto.getCodigo());
        region.setActivo(regionDto.isActivo());
        
        return regionRepository.save(region);
    }
    
    // Eliminar una región (desactivar)
    @Transactional
    public void desactivarRegion(Long id) {
        Region region = obtenerRegionPorId(id);
        region.setActivo(false);
        regionRepository.save(region);
    }
    
    // Activar una región
    @Transactional
    public void activarRegion(Long id) {
        Region region = obtenerRegionPorId(id);
        region.setActivo(true);
        regionRepository.save(region);
    }
}