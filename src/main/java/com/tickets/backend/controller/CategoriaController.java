package com.tickets.backend.controller;

import com.tickets.backend.dto.CategoriaDto;
import com.tickets.backend.dto.SubcategoriaDto;
import com.tickets.backend.models.Categoria;
import com.tickets.backend.models.Subcategoria;
import com.tickets.backend.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // Método para convertir Categoria a CategoriaDto
    private CategoriaDto convertirADto(Categoria categoria) {
        return new CategoriaDto(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            categoria.isActivo()
        );
    }

    // Método para convertir Subcategoria a SubcategoriaDto
    private SubcategoriaDto convertirADto(Subcategoria subcategoria) {
        return new SubcategoriaDto(
            subcategoria.getId(),
            subcategoria.getNombre(),
            subcategoria.getDescripcion(),
            subcategoria.getCategoria().getId(),
            subcategoria.isActivo()
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDto>> obtenerCategorias() {
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas();
        List<CategoriaDto> categoriasDto = categorias.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoriasDto);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaDto>> obtenerCategoriasActivas() {
        List<Categoria> categorias = categoriaService.obtenerCategoriasActivas();
        List<CategoriaDto> categoriasDto = categorias.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoriasDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDto> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.obtenerCategoriaPorId(id);
        return ResponseEntity.ok(convertirADto(categoria));
    }

    @PostMapping
    public ResponseEntity<CategoriaDto> crearCategoria(@Valid @RequestBody CategoriaDto categoriaDto) {
        Categoria nuevaCategoria = categoriaService.crearCategoria(categoriaDto);
        return new ResponseEntity<>(convertirADto(nuevaCategoria), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDto> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDto categoriaDto) {
        
        Categoria categoriaActualizada = categoriaService.actualizarCategoria(id, categoriaDto);
        return ResponseEntity.ok(convertirADto(categoriaActualizada));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoCategoria(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        
        boolean activo = payload.get("activo");
        categoriaService.cambiarEstadoCategoria(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints para Subcategorías

    @GetMapping("/{categoriaId}/subcategorias")
    public ResponseEntity<List<SubcategoriaDto>> obtenerSubcategoriasPorCategoria(
            @PathVariable Long categoriaId) {
        
        List<Subcategoria> subcategorias = categoriaService.obtenerSubcategoriasPorCategoria(categoriaId);
        List<SubcategoriaDto> subcategoriasDto = subcategorias.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subcategoriasDto);
    }

    @GetMapping("/{categoriaId}/subcategorias/activas")
    public ResponseEntity<List<SubcategoriaDto>> obtenerSubcategoriasActivasPorCategoria(
            @PathVariable Long categoriaId) {
        
        List<Subcategoria> subcategorias = categoriaService.obtenerSubcategoriasActivasPorCategoria(categoriaId);
        List<SubcategoriaDto> subcategoriasDto = subcategorias.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subcategoriasDto);
    }

    @GetMapping("/subcategorias/{id}")
    public ResponseEntity<SubcategoriaDto> obtenerSubcategoriaPorId(@PathVariable Long id) {
        Subcategoria subcategoria = categoriaService.obtenerSubcategoriaPorId(id);
        return ResponseEntity.ok(convertirADto(subcategoria));
    }

    @PostMapping("/{categoriaId}/subcategorias")
    public ResponseEntity<SubcategoriaDto> crearSubcategoria(
            @PathVariable Long categoriaId,
            @Valid @RequestBody SubcategoriaDto subcategoriaDto) {
        
        Subcategoria nuevaSubcategoria = categoriaService.crearSubcategoria(categoriaId, subcategoriaDto);
        return new ResponseEntity<>(convertirADto(nuevaSubcategoria), HttpStatus.CREATED);
    }

    @PutMapping("/subcategorias/{id}")
    public ResponseEntity<SubcategoriaDto> actualizarSubcategoria(
            @PathVariable Long id,
            @Valid @RequestBody SubcategoriaDto subcategoriaDto) {
        
        Subcategoria subcategoriaActualizada = categoriaService.actualizarSubcategoria(id, subcategoriaDto);
        return ResponseEntity.ok(convertirADto(subcategoriaActualizada));
    }

    @PutMapping("/subcategorias/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoSubcategoria(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        
        boolean activo = payload.get("activo");
        categoriaService.cambiarEstadoSubcategoria(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subcategorias/{id}")
    public ResponseEntity<Void> eliminarSubcategoria(@PathVariable Long id) {
        categoriaService.eliminarSubcategoria(id);
        return ResponseEntity.noContent().build();
    }
}