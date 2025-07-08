package com.tickets.backend.service;

import com.tickets.backend.exceptions.ResourceNotFoundException;
import com.tickets.backend.models.SupervisorTecnico;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.SupervisorTecnicoRepository;
import com.tickets.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupervisorTecnicoService {
    
    @Autowired
    private SupervisorTecnicoRepository supervisorTecnicoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Asignar un técnico a un supervisor
     */
    public SupervisorTecnico asignarTecnicoASupervisor(Long supervisorId, Long tecnicoId) {
        Usuario supervisor = usuarioRepository.findById(supervisorId)
            .orElseThrow(() -> new ResourceNotFoundException("Supervisor no encontrado"));
        
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
            .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        
        // Validar que el supervisor tiene el rol correcto
        if (!supervisor.esSupervisor()) {
            throw new IllegalArgumentException("El usuario no tiene el rol de supervisor");
        }
        
        // Validar que el técnico tiene el rol correcto
        if (!tecnico.esTecnico()) {
            throw new IllegalArgumentException("El usuario no tiene el rol de técnico");
        }
        
        // Validar que están en la misma categoría
        if (supervisor.getCategoria() == null || tecnico.getCategoria() == null) {
            throw new IllegalArgumentException("Supervisor y técnico deben tener categorías asignadas");
        }
        
        if (!supervisor.getCategoria().getId().equals(tecnico.getCategoria().getId())) {
            throw new IllegalArgumentException("Supervisor y técnico deben pertenecer a la misma categoría");
        }
        
        // Verificar si ya existe la relación
        supervisorTecnicoRepository.findBySupervisorAndTecnicoAndActivoTrue(supervisor, tecnico)
            .ifPresent(st -> {
                throw new IllegalArgumentException("El técnico ya está asignado a este supervisor");
            });
        
        SupervisorTecnico supervisorTecnico = new SupervisorTecnico(supervisor, tecnico);
        return supervisorTecnicoRepository.save(supervisorTecnico);
    }
    
    /**
     * Remover la asignación de un técnico de un supervisor
     */
    public void removerTecnicoDeSupervisor(Long supervisorId, Long tecnicoId) {
        Usuario supervisor = usuarioRepository.findById(supervisorId)
            .orElseThrow(() -> new ResourceNotFoundException("Supervisor no encontrado"));
        
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
            .orElseThrow(() -> new ResourceNotFoundException("Técnico no encontrado"));
        
        SupervisorTecnico supervisorTecnico = supervisorTecnicoRepository
            .findBySupervisorAndTecnicoAndActivoTrue(supervisor, tecnico)
            .orElseThrow(() -> new ResourceNotFoundException("Relación supervisor-técnico no encontrada"));
        
        supervisorTecnico.setActivo(false);
        supervisorTecnicoRepository.save(supervisorTecnico);
    }
    
    /**
     * Obtener todos los técnicos de un supervisor
     */
    public List<Usuario> obtenerTecnicosDeSupervisor(Long supervisorId) {
        return supervisorTecnicoRepository.findTecnicosUsuariosBySupervisor(supervisorId);
    }
    
    /**
     * Obtener todos los supervisores de un técnico
     */
    public List<Usuario> obtenerSupervisoresDeTecnico(Long tecnicoId) {
        return supervisorTecnicoRepository.findSupervisoresByTecnico(tecnicoId)
            .stream()
            .map(SupervisorTecnico::getSupervisor)
            .collect(Collectors.toList());
    }
    
    /**
     * Verificar si un usuario puede asignar tickets a otro usuario
     */
    public boolean puedeAsignarTicket(Long asignadorId, Long tecnicoId) {
        Usuario asignador = usuarioRepository.findById(asignadorId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario asignador no encontrado"));
        
        // Si es admin, puede asignar a cualquiera
        if (asignador.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("ROLE_ADMIN"))) {
            return true;
        }
        
        // Si es supervisor, solo puede asignar a sus técnicos o a sí mismo
        if (asignador.esSupervisor()) {
            // Puede asignarse a sí mismo
            if (asignadorId.equals(tecnicoId)) {
                return true;
            }
            // Puede asignar a sus técnicos
            return supervisorTecnicoRepository.existsSupervisorTecnico(asignadorId, tecnicoId);
        }
        
        // Si es técnico, solo puede asignarse a sí mismo
        if (asignador.esTecnico()) {
            return asignadorId.equals(tecnicoId);
        }
        
        return false;
    }
    
    /**
     * Obtener técnicos disponibles para asignación según usuario actual
     */
    public List<Usuario> obtenerTecnicosDisponibles(Long usuarioId, Long categoriaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Si es admin, obtener todos los técnicos de la categoría
        if (usuario.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("ROLE_ADMIN"))) {
            return usuarioRepository.findByRolesNombreAndEspecialidadAndActivoTrue("ROLE_TECNICO", categoriaId.toString());
        }
        
        // Si es supervisor, obtener sus técnicos + sí mismo (si es técnico también)
        if (usuario.esSupervisor()) {
            List<Usuario> tecnicos = supervisorTecnicoRepository
                .findTecnicosBySupervisorAndCategoria(usuarioId, categoriaId);
            
            // Si el supervisor también es técnico, agregarlo a la lista
            if (usuario.esTecnico() && usuario.getCategoria() != null && 
                usuario.getCategoria().getId().equals(categoriaId)) {
                if (!tecnicos.contains(usuario)) {
                    tecnicos.add(usuario);
                }
            }
            
            return tecnicos;
        }
        
        // Si es técnico, solo él mismo
        if (usuario.esTecnico() && usuario.getCategoria() != null && 
            usuario.getCategoria().getId().equals(categoriaId)) {
            return List.of(usuario);
        }
        
        return List.of();
    }
}