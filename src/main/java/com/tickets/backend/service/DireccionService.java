package com.tickets.backend.service;

import com.tickets.backend.models.Comuna;
import com.tickets.backend.models.Direccion;
import com.tickets.backend.repository.ComunaRepository;
import com.tickets.backend.repository.DireccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private ComunaRepository comunaRepository;

    /**
     * Obtiene todas las direcciones registradas
     */
    public List<Direccion> obtenerTodas() {
        return direccionRepository.findAll();
    }

    /**
     * Obtiene una dirección por su ID
     */
    public Direccion obtenerPorId(Long id) {
        return direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada con ID: " + id));
    }

    /**
     * Obtiene todas las direcciones de una comuna específica
     */
    public List<Direccion> obtenerPorComunaId(Long comunaId) {
        Comuna comuna = comunaRepository.findById(comunaId)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + comunaId));
        return direccionRepository.findByComunaId(comuna.getId());
    }

    /**
     * Guarda una nueva dirección
     */
    @Transactional
    public Direccion guardar(Direccion direccion) {
        if (direccion.getComuna() != null && direccion.getComuna().getId() != null) {
            Comuna comuna = comunaRepository.findById(direccion.getComuna().getId())
                    .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + direccion.getComuna().getId()));
            direccion.setComuna(comuna);
        }
        return direccionRepository.save(direccion);
    }

    /**
     * Actualiza una dirección existente
     */
    @Transactional
    public Direccion actualizar(Long id, Direccion direccionActualizada) {
        if (direccionRepository.existsById(id)) {
            if (direccionActualizada.getComuna() != null && direccionActualizada.getComuna().getId() != null) {
                Comuna comuna = comunaRepository.findById(direccionActualizada.getComuna().getId())
                        .orElseThrow(() -> new RuntimeException("Comuna no encontrada con ID: " + direccionActualizada.getComuna().getId()));
                direccionActualizada.setComuna(comuna);
            }
            direccionActualizada.setId(id);
            return direccionRepository.save(direccionActualizada);
        } else {
            throw new RuntimeException("Dirección no encontrada con ID: " + id);
        }
    }

    /**
     * Elimina una dirección por su ID
     */
    @Transactional
    public void eliminar(Long id) {
        if (direccionRepository.existsById(id)) {
            direccionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Dirección no encontrada con ID: " + id);
        }
    }

    /**
     * Busca direcciones por calle
     */
    public List<Direccion> buscarPorCalle(String calle) {
        return direccionRepository.findByCalleContaining(calle);
    }

}