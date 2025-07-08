package com.tickets.backend.service;

import com.tickets.backend.dto.CategoriaDto;
import com.tickets.backend.dto.SubcategoriaDto;
import com.tickets.backend.models.Categoria;
import com.tickets.backend.models.Subcategoria;
import com.tickets.backend.repository.CategoriaRepository;
import com.tickets.backend.repository.SubcategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    public List<Categoria> obtenerTodasCategorias() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> obtenerCategoriasActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    public Categoria obtenerCategoriaPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    @Transactional
    public Categoria crearCategoria(CategoriaDto categoriaDto) {
        if (categoriaRepository.existsByNombre(categoriaDto.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con este nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDto.getNombre());
        categoria.setDescripcion(categoriaDto.getDescripcion());
        categoria.setActivo(true);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizarCategoria(Long id, CategoriaDto categoriaDto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Verificar que el nombre no esté en uso por otra categoría
        if (!categoria.getNombre().equals(categoriaDto.getNombre()) && 
                categoriaRepository.existsByNombre(categoriaDto.getNombre())) {
            throw new RuntimeException("Ya existe otra categoría con este nombre");
        }

        categoria.setNombre(categoriaDto.getNombre());
        categoria.setDescripcion(categoriaDto.getDescripcion());

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void cambiarEstadoCategoria(Long id, boolean activo) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        categoria.setActivo(activo);
        categoriaRepository.save(categoria);
        
        // Si la categoría está inactiva, también marcar como inactivas sus subcategorías
        if (!activo) {
            List<Subcategoria> subcategorias = subcategoriaRepository.findByCategoriaId(id);
            for (Subcategoria subcategoria : subcategorias) {
                subcategoria.setActivo(false);
            }
            subcategoriaRepository.saveAll(subcategorias);
        }
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        // Verificar si existen tickets asociados a esta categoría antes de eliminar
        
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        
        // Primero eliminar todas las subcategorías asociadas
        List<Subcategoria> subcategorias = subcategoriaRepository.findByCategoriaId(id);
        subcategoriaRepository.deleteAll(subcategorias);
        
        categoriaRepository.deleteById(id);
    }

    // Métodos para Subcategorías
    
    public List<Subcategoria> obtenerSubcategoriasPorCategoria(Long categoriaId) {
        return subcategoriaRepository.findByCategoriaId(categoriaId);
    }
    
    public List<Subcategoria> obtenerSubcategoriasActivasPorCategoria(Long categoriaId) {
        return subcategoriaRepository.findByCategoriaIdAndActivoTrue(categoriaId);
    }
    
    public Subcategoria obtenerSubcategoriaPorId(Long id) {
        return subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
    }
    
    @Transactional
    public Subcategoria crearSubcategoria(Long categoriaId, SubcategoriaDto subcategoriaDto) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        if (!categoria.isActivo()) {
            throw new RuntimeException("No se puede agregar subcategoría a una categoría inactiva");
        }
        
        if (subcategoriaRepository.existsByNombreAndCategoriaId(subcategoriaDto.getNombre(), categoriaId)) {
            throw new RuntimeException("Ya existe una subcategoría con este nombre en esta categoría");
        }
        
        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setNombre(subcategoriaDto.getNombre());
        subcategoria.setDescripcion(subcategoriaDto.getDescripcion());
        subcategoria.setCategoria(categoria);
        subcategoria.setActivo(true);
        
        return subcategoriaRepository.save(subcategoria);
    }
    
    @Transactional
    public Subcategoria actualizarSubcategoria(Long id, SubcategoriaDto subcategoriaDto) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        
        // Verificar que el nombre no esté en uso por otra subcategoría en la misma categoría
        if (!subcategoria.getNombre().equals(subcategoriaDto.getNombre()) && 
                subcategoriaRepository.existsByNombreAndCategoriaId(
                        subcategoriaDto.getNombre(), subcategoria.getCategoria().getId())) {
            throw new RuntimeException("Ya existe otra subcategoría con este nombre en esta categoría");
        }
        
        subcategoria.setNombre(subcategoriaDto.getNombre());
        subcategoria.setDescripcion(subcategoriaDto.getDescripcion());
        
        return subcategoriaRepository.save(subcategoria);
    }
    
    @Transactional
    public void cambiarEstadoSubcategoria(Long id, boolean activo) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategoría no encontrada"));
        
        subcategoria.setActivo(activo);
        subcategoriaRepository.save(subcategoria);
    }
    
    @Transactional
    public void eliminarSubcategoria(Long id) {
        // Verificar si existen tickets asociados a esta subcategoría antes de eliminar
        
        if (!subcategoriaRepository.existsById(id)) {
            throw new RuntimeException("Subcategoría no encontrada");
        }
        
        subcategoriaRepository.deleteById(id);
    }
}