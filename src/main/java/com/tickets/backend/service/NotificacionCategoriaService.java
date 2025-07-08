// NotificacionCategoriaService.java
package com.tickets.backend.service;

import com.tickets.backend.dto.NotificacionCategoriaDto;
import com.tickets.backend.models.Categoria;
import com.tickets.backend.models.NotificacionCategoria;
import com.tickets.backend.repository.CategoriaRepository;
import com.tickets.backend.repository.NotificacionCategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionCategoriaService {

    @Autowired
    private NotificacionCategoriaRepository notificacionCategoriaRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public List<NotificacionCategoria> obtenerNotificacionesPorCategoria(Long categoriaId) {
        return notificacionCategoriaRepository.findByCategoriaIdAndActivoTrue(categoriaId);
    }
    
    public List<String> obtenerEmailsNotificacionPorCategoria(Long categoriaId) {
        return notificacionCategoriaRepository.findByCategoriaIdAndActivoTrue(categoriaId)
                .stream()
                .map(NotificacionCategoria::getEmail)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificacionCategoria agregarNotificacion(NotificacionCategoriaDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        // Validar que el email no esté duplicado para esta categoría
        List<NotificacionCategoria> existentes = notificacionCategoriaRepository.findByCategoriaIdAndActivoTrue(dto.getCategoriaId());
        boolean emailDuplicado = existentes.stream()
                .anyMatch(n -> n.getEmail().equalsIgnoreCase(dto.getEmail()));
        
        if (emailDuplicado) {
            throw new RuntimeException("Este email ya está configurado para esta categoría");
        }
        
        NotificacionCategoria notificacion = new NotificacionCategoria();
        notificacion.setCategoria(categoria);
        notificacion.setEmail(dto.getEmail());
        notificacion.setDescripcion(dto.getDescripcion());
        notificacion.setActivo(true);
        
        return notificacionCategoriaRepository.save(notificacion);
    }
    
    @Transactional
    public NotificacionCategoria actualizarNotificacion(Long id, NotificacionCategoriaDto dto) {
        NotificacionCategoria notificacion = notificacionCategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        // Si cambia el email, verificar que no esté duplicado
        if (!notificacion.getEmail().equalsIgnoreCase(dto.getEmail())) {
            List<NotificacionCategoria> existentes = notificacionCategoriaRepository.findByCategoriaIdAndActivoTrue(notificacion.getCategoria().getId());
            boolean emailDuplicado = existentes.stream()
                    .anyMatch(n -> n.getEmail().equalsIgnoreCase(dto.getEmail()));
            
            if (emailDuplicado) {
                throw new RuntimeException("Este email ya está configurado para esta categoría");
            }
        }
        
        notificacion.setEmail(dto.getEmail());
        notificacion.setDescripcion(dto.getDescripcion());
        
        return notificacionCategoriaRepository.save(notificacion);
    }
    
    @Transactional
    public void desactivarNotificacion(Long id) {
        NotificacionCategoria notificacion = notificacionCategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        notificacion.setActivo(false);
        notificacionCategoriaRepository.save(notificacion);
    }
    
    @Transactional
    public void eliminarNotificacion(Long id) {
        if (!notificacionCategoriaRepository.existsById(id)) {
            throw new RuntimeException("Notificación no encontrada");
        }
        
        notificacionCategoriaRepository.deleteById(id);
    }
}