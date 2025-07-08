package com.tickets.backend.service;

import com.tickets.backend.dto.EmpresaDto;
import com.tickets.backend.dto.SucursalDto;
import com.tickets.backend.models.Empresa;
import com.tickets.backend.models.Sucursal;
import com.tickets.backend.repository.EmpresaRepository;
import com.tickets.backend.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    // Métodos para Empresas
    public List<Empresa> obtenerTodasEmpresas() {
        return empresaRepository.findAll();
    }

    public List<Empresa> obtenerEmpresasActivas() {
        return empresaRepository.findByActivoTrue();
    }

    public Empresa obtenerEmpresaPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
    }

    @Transactional
    public Empresa crearEmpresa(EmpresaDto empresaDto) {
        // Verificar si el nombre ya existe
        if (empresaRepository.existsByNombre(empresaDto.getNombre())) {
            throw new RuntimeException("Ya existe una empresa con este nombre");
        }

        // Verificar si el RUT ya existe
        if (empresaDto.getRut() != null && empresaRepository.existsByRut(empresaDto.getRut())) {
            throw new RuntimeException("Ya existe una empresa con este RUT");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(empresaDto.getNombre());
        empresa.setDescripcion(empresaDto.getDescripcion());
        empresa.setRut(empresaDto.getRut());
        empresa.setActivo(true);

        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa actualizarEmpresa(Long id, EmpresaDto empresaDto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // Verificar que el nombre no esté en uso por otra empresa
        if (!empresa.getNombre().equals(empresaDto.getNombre()) && 
                empresaRepository.existsByNombre(empresaDto.getNombre())) {
            throw new RuntimeException("Ya existe otra empresa con este nombre");
        }

        // Verificar que el RUT no esté en uso por otra empresa
        if (empresaDto.getRut() != null && !empresaDto.getRut().equals(empresa.getRut()) && 
                empresaRepository.existsByRut(empresaDto.getRut())) {
            throw new RuntimeException("Ya existe otra empresa con este RUT");
        }

        empresa.setNombre(empresaDto.getNombre());
        empresa.setDescripcion(empresaDto.getDescripcion());
        empresa.setRut(empresaDto.getRut());

        return empresaRepository.save(empresa);
    }

    @Transactional
    public void eliminarEmpresa(Long id) {
        cambiarEstadoEmpresa(id, false);
    }

    @Transactional
    public void activarEmpresa(Long id) {
        cambiarEstadoEmpresa(id, true);
    }

    @Transactional
    public void cambiarEstadoEmpresa(Long id, boolean activo) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        empresa.setActivo(activo);
        empresaRepository.save(empresa);
        
        // Si la empresa está inactiva, también marcar como inactivas sus sucursales
        if (!activo) {
            List<Sucursal> sucursales = sucursalRepository.findByEmpresaId(id);
            for (Sucursal sucursal : sucursales) {
                sucursal.setActivo(false);
            }
            sucursalRepository.saveAll(sucursales);
        }
    }

    // Métodos para Sucursales
    public List<Sucursal> obtenerSucursalesPorEmpresa(Long empresaId) {
        return sucursalRepository.findByEmpresaId(empresaId);
    }
    
    public List<Sucursal> obtenerSucursalesActivasPorEmpresa(Long empresaId) {
        return sucursalRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }
    
    public Sucursal obtenerSucursalPorId(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
    }
    
    @Transactional
    public Sucursal crearSucursal(Long empresaId, SucursalDto sucursalDto) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        
        if (!empresa.isActivo()) {
            throw new RuntimeException("No se puede agregar sucursal a una empresa inactiva");
        }
        
        if (sucursalRepository.existsByNombreAndEmpresaId(sucursalDto.getNombre(), empresaId)) {
            throw new RuntimeException("Ya existe una sucursal con este nombre en esta empresa");
        }
        
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(sucursalDto.getNombre());
        
        // Actualización para usar la nueva estructura de dirección
        if (sucursalDto.getDireccion() != null) {
            // Si se proporciona un ID de dirección existente, asignarlo
            // Aquí necesitarías el DireccionService para obtener la dirección por ID
            // Ejemplo: sucursal.setDireccion(direccionService.obtenerPorId(sucursalDto.getDireccionId()));
        } else if (sucursalDto.getDireccion() != null) {
            // Si se proporciona una nueva dirección, usarla
            sucursal.setDireccion(sucursalDto.getDireccion());
        }
        
        sucursal.setTelefono(sucursalDto.getTelefono());
        sucursal.setEmpresa(empresa);
        sucursal.setActivo(true);
        
        return sucursalRepository.save(sucursal);
    }
    
    @Transactional
public Sucursal actualizarSucursal(Long id, SucursalDto sucursalDto) {
    Sucursal sucursal = sucursalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
    
    // Verificar que el nombre no esté en uso por otra sucursal en la misma empresa
    if (!sucursal.getNombre().equals(sucursalDto.getNombre()) && 
            sucursalRepository.existsByNombreAndEmpresaId(
                    sucursalDto.getNombre(), sucursal.getEmpresa().getId())) {
        throw new RuntimeException("Ya existe otra sucursal con este nombre en esta empresa");
    }
    
    sucursal.setNombre(sucursalDto.getNombre());
    
    // Actualización para usar la nueva estructura de dirección
    if (sucursalDto.getDireccionId() != null) {
        // Si se proporciona un ID de dirección existente, actualizar la referencia
        // Ejemplo: sucursal.setDireccion(direccionService.obtenerPorId(sucursalDto.getDireccionId()));
    } else if (sucursalDto.getDireccion() != null) {
        // Si se proporciona una nueva dirección, actualizarla
        // Si la sucursal ya tenía una dirección, actualizar sus propiedades
        if (sucursal.getDireccion() != null) {
            // Actualizar la dirección existente
            sucursal.getDireccion().setCalle(sucursalDto.getDireccion().getCalle());
            sucursal.getDireccion().setNumero(sucursalDto.getDireccion().getNumero());
            // La dirección no tiene relación directa con Región, se asigna a través de Comuna
            sucursal.getDireccion().setComuna(sucursalDto.getDireccion().getComuna());
            
        } else {
            // Asignar una nueva dirección
            sucursal.setDireccion(sucursalDto.getDireccion());
        }
    }
    
    sucursal.setTelefono(sucursalDto.getTelefono());
    
    return sucursalRepository.save(sucursal);
}
    
    @Transactional
    public void eliminarSucursal(Long id) {
        cambiarEstadoSucursal(id, false);
    }
    
    @Transactional
    public void activarSucursal(Long id) {
        cambiarEstadoSucursal(id, true);
    }
    
    @Transactional
    public void cambiarEstadoSucursal(Long id, boolean activo) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        
        // Si estamos activando una sucursal, verificar que la empresa esté activa
        if (activo && !sucursal.getEmpresa().isActivo()) {
            throw new RuntimeException("No se puede activar una sucursal de una empresa inactiva");
        }
        
        sucursal.setActivo(activo);
        sucursalRepository.save(sucursal);
    }
}