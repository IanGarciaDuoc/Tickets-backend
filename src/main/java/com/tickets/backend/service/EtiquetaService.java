package com.tickets.backend.service;

import com.tickets.backend.models.Etiqueta;
import com.tickets.backend.repository.EtiquetaRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;

   
    public EtiquetaService(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    public List<Etiqueta> obtenerTodasLasEtiquetas() {
        return etiquetaRepository.findAll();
    }

    public Optional<Etiqueta> obtenerEtiquetaPorId(Long id) {
        return etiquetaRepository.findById(id);
    }

    public Optional<Etiqueta> buscarPorNombre(String nombre) {
        return etiquetaRepository.findByNombre(nombre);
    }

    public List<Etiqueta> buscarPorPalabraClave(String keyword) {
        return etiquetaRepository.searchByKeyword(keyword);
    }

    public List<Etiqueta> obtenerEtiquetasPorTicket(Long ticketId) {
        return etiquetaRepository.findByTicketId(ticketId);
    }

    public Etiqueta guardarEtiqueta(Etiqueta etiqueta) {
        return etiquetaRepository.save(etiqueta);
    }

    public void eliminarEtiqueta(Long id) {
        etiquetaRepository.deleteById(id);
    }

    public boolean existePorNombre(String nombre) {
        return etiquetaRepository.existsByNombre(nombre);
    }
}