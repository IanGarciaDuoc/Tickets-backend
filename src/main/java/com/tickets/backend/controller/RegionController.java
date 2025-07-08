package com.tickets.backend.controller;

import com.tickets.backend.dto.ComunaDto;
import com.tickets.backend.dto.PaisDto;
import com.tickets.backend.dto.RegionDto;
import com.tickets.backend.dto.RegionResponseDto;
import com.tickets.backend.models.Region;
import com.tickets.backend.service.RegionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regiones")
@CrossOrigin(origins = "*")
public class RegionController {

    @Autowired
    private RegionService regionService;

    public ResponseEntity<List<RegionResponseDto>> obtenerTodas() {
    List<Region> regiones = regionService.obtenerTodasRegiones();
    List<RegionResponseDto> dtos = regiones.stream()
            .map(region -> convertToResponseDto(region))
            .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}

   @GetMapping("/activas")
public ResponseEntity<List<RegionResponseDto>> obtenerActivas() {
    List<Region> regiones = regionService.obtenerRegionesActivas();
    List<RegionResponseDto> dtos = regiones.stream()
            .map(region -> convertToResponseDto(region))
            .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
}
private RegionResponseDto convertToResponseDto(Region region) {
    RegionResponseDto dto = new RegionResponseDto();
    dto.setId(region.getId());
    dto.setNombre(region.getNombre());
    dto.setCodigo(region.getCodigo());
    dto.setActivo(region.isActivo());
    
    // Convertir País a PaisDto si existe
    if (region.getPais() != null) {
        PaisDto paisDto = new PaisDto();
        paisDto.setId(region.getPais().getId());
        paisDto.setNombre(region.getPais().getNombre());
        paisDto.setCodigo(region.getPais().getCodigo());
        dto.setPais(paisDto);
    }
    
    // Convertir Comunas a ComunaDto si existen
    if (region.getComunas() != null && !region.getComunas().isEmpty()) {
        List<ComunaDto> comunaDtos = region.getComunas().stream()
                .map(comuna -> {
                    ComunaDto comunaDto = new ComunaDto();
                    comunaDto.setId(comuna.getId());
                    comunaDto.setNombre(comuna.getNombre());
                    comunaDto.setCodigo(comuna.getCodigo());
                    comunaDto.setActivo(comuna.isActivo());
                    return comunaDto;
                })
                .collect(Collectors.toList());
        dto.setComunas(comunaDtos);
    }
    
    return dto;
}

    @GetMapping("/{id}")
public ResponseEntity<RegionResponseDto> obtenerPorId(@PathVariable Long id) {
    Region region = regionService.obtenerRegionPorId(id);
    RegionResponseDto dto = convertToResponseDto(region);
    return ResponseEntity.ok(dto);
}
    @GetMapping("/pais/{paisId}")
    public ResponseEntity<List<Region>> obtenerPorPais(@PathVariable Long paisId) {
        return ResponseEntity.ok(regionService.obtenerRegionesPorPais(paisId));
    }

    @GetMapping("/pais/{paisId}/activas")
    public ResponseEntity<List<Region>> obtenerActivasPorPais(@PathVariable Long paisId) {
        return ResponseEntity.ok(regionService.obtenerRegionesActivasPorPais(paisId));
    }

    @PostMapping
    public ResponseEntity<Region> crear(@Valid @RequestBody RegionDto regionDto) {
        return new ResponseEntity<>(regionService.crearRegion(regionDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Region> actualizar(@PathVariable Long id, @Valid @RequestBody RegionDto regionDto) {
        return ResponseEntity.ok(regionService.actualizarRegion(id, regionDto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        regionService.desactivarRegion(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        regionService.activarRegion(id);
        return ResponseEntity.noContent().build();
    }
}