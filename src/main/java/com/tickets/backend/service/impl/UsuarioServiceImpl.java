// src/main/java/com/tickets/backend/service/impl/UsuarioServiceImpl.java
package com.tickets.backend.service.impl;

import com.tickets.backend.dto.UsuarioDto;
import com.tickets.backend.exceptions.ResourceNotFoundException;
import com.tickets.backend.models.Categoria;
import com.tickets.backend.models.Empresa;
import com.tickets.backend.models.Rol;
import com.tickets.backend.models.Sucursal;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.CategoriaRepository;
import com.tickets.backend.repository.EmpresaRepository;
import com.tickets.backend.repository.RolRepository;
import com.tickets.backend.repository.SucursalRepository;
import com.tickets.backend.repository.UsuarioRepository;
import com.tickets.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private SucursalRepository sucursalRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // OPTIMIZADO: Usar consulta optimizada con fetch joins
    @Override
    public Page<Usuario> obtenerTodosUsuarios(Pageable pageable) {
        return usuarioRepository.findAllOptimized(pageable);
    }

    // OPTIMIZADO: Usar consultas optimizadas
    @Override
    @Cacheable(value = "usuariosActivos", unless = "#result.size() > 100")
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByRolesNombreAndActivoTrueOptimized("ROLE_USER"); // Usar método optimizado
    }

    @Override
    @Cacheable(value = "usuariosInactivos", unless = "#result.size() > 100")
    public List<Usuario> obtenerUsuariosInactivos() {
        return usuarioRepository.findByActivoFalse();
    }

    // OPTIMIZADO: Usar consulta optimizada por ID
    @Override
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findByIdOptimized(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"usuariosActivos", "usuariosInactivos"}, allEntries = true)
    public Usuario crearUsuario(UsuarioDto usuarioDto) {
        // Verificar si ya existe un usuario con ese email
        if(usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuarioDto.getEmail());
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDto.getNombre());
        usuario.setApellido(usuarioDto.getApellido());
        usuario.setEmail(usuarioDto.getEmail());
        
        // Encriptar la contraseña si se proporciona
        if(usuarioDto.getPassword() != null && !usuarioDto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDto.getPassword()));
        }
        
        usuario.setEspecialidad(usuarioDto.getEspecialidad());
        usuario.setActivo(usuarioDto.isActivo());
        usuario.setFechaCreacion(LocalDateTime.now());
        
        // OPTIMIZADO: Usar batch loading para entidades relacionadas
        setRelatedEntities(usuario, usuarioDto);
        
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"usuariosActivos", "usuariosInactivos"}, allEntries = true)
    public Usuario actualizarUsuario(Long id, UsuarioDto usuarioDto) {
        // OPTIMIZADO: Usar consulta optimizada
        Usuario usuario = obtenerUsuarioPorId(id);
        
        // Verificar si el email existe pero pertenece a otro usuario
        if(!usuario.getEmail().equals(usuarioDto.getEmail()) && usuarioRepository.existsByEmail(usuarioDto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuarioDto.getEmail());
        }
        
        usuario.setNombre(usuarioDto.getNombre());
        usuario.setApellido(usuarioDto.getApellido());
        usuario.setEmail(usuarioDto.getEmail());
        
        // Actualizar contraseña solo si se proporciona
        if(usuarioDto.getPassword() != null && !usuarioDto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDto.getPassword()));
        }
        
        usuario.setEspecialidad(usuarioDto.getEspecialidad());
        usuario.setActivo(usuarioDto.isActivo());
        
        // OPTIMIZADO: Usar método helper para entidades relacionadas
        setRelatedEntities(usuario, usuarioDto);
        
        return usuarioRepository.save(usuario);
    }

    // NUEVO: Método helper para optimizar la asignación de entidades relacionadas
    private void setRelatedEntities(Usuario usuario, UsuarioDto usuarioDto) {
        // Asignar empresa si se proporciona ID
        if(usuarioDto.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(usuarioDto.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + usuarioDto.getEmpresaId()));
            usuario.setEmpresa(empresa);
        } else {
            usuario.setEmpresa(null);
        }
        
        // Asignar sucursal si se proporciona ID
        if(usuarioDto.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(usuarioDto.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + usuarioDto.getSucursalId()));
            usuario.setSucursal(sucursal);
        } else {
            usuario.setSucursal(null);
        }
        
        // Asignar categoría si se proporciona ID
        if(usuarioDto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(usuarioDto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + usuarioDto.getCategoriaId()));
            usuario.setCategoria(categoria);
        } else {
            usuario.setCategoria(null);
        }
        
        // OPTIMIZADO: Batch load roles
        if(usuarioDto.getRolesId() != null && !usuarioDto.getRolesId().isEmpty()) {
            List<Rol> rolesList = rolRepository.findAllById(usuarioDto.getRolesId());
            if(rolesList.size() != usuarioDto.getRolesId().size()) {
                throw new ResourceNotFoundException("Uno o más roles no fueron encontrados");
            }
            usuario.setRoles(new HashSet<>(rolesList));
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"usuariosActivos", "usuariosInactivos"}, allEntries = true)
    public void desactivarUsuario(Long id) {
        Usuario usuario = obtenerUsuarioPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"usuariosActivos", "usuariosInactivos"}, allEntries = true)
    public void eliminarUsuario(Long id) {
        if(!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
    
    @Override
    @CacheEvict(value = {"usuariosActivos", "usuariosInactivos"}, allEntries = true)
    public void activarUsuario(Long id) {
        Usuario usuario = obtenerUsuarioPorId(id);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
    
    // OPTIMIZADO: Usar consulta optimizada
    @Override
    @Cacheable(value = "tecnicosActivos")
    public List<Usuario> obtenerTecnicosActivos() {
        return usuarioRepository.findByRolesNombreAndActivoTrueOptimized("ROLE_TECNICO");
    }

    @Override
    public List<Usuario> obtenerTecnicosPorEspecialidad(String especialidad) {
        return usuarioRepository.findByRolesNombreAndEspecialidadAndActivoTrue("ROLE_TECNICO", especialidad);
    }
    
    // OPTIMIZADO: Usar consulta optimizada
    @Override
    @Cacheable(value = "tecnicosPorCategoria", key = "#categoriaId")
    public List<Usuario> obtenerTecnicosPorCategoria(Long categoriaId) {
        return usuarioRepository.findByRolesNombreAndCategoriaIdAndActivoTrueOptimized("ROLE_TECNICO", categoriaId);
    }
    
    @Override
    @Cacheable(value = "supervisoresPorCategoria", key = "#categoriaId")
    public List<Usuario> obtenerSupervisoresPorCategoria(Long categoriaId) {
        return usuarioRepository.findByRolesNombreAndCategoriaIdAndActivoTrueOptimized("ROLE_SUPERVISOR", categoriaId);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"usuariosActivos", "usuariosInactivos", "tecnicosActivos"}, allEntries = true)
    public Usuario asignarRolesAUsuario(Long usuarioId, Set<Long> rolesIds) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        
        // OPTIMIZADO: Batch load roles
        List<Rol> rolesList = rolRepository.findAllById(rolesIds);
        if(rolesList.size() != rolesIds.size()) {
            throw new ResourceNotFoundException("Uno o más roles no fueron encontrados");
        }
        
        usuario.setRoles(new HashSet<>(rolesList));
        return usuarioRepository.save(usuario);
    }

    // OPTIMIZADO: Método principal con consultas optimizadas
    @Override
    public Page<Usuario> buscarUsuarios(Pageable pageable, Boolean activo, String search, Long empresaId, Long rolId) {
        try {
            // Limpiar el término de búsqueda
            String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
            
            // OPTIMIZADO: Usar consultas optimizadas con fetch joins
            if (empresaId == null && rolId == null) {
                return usuarioRepository.buscarUsuariosSimpleOptimizado(activo, searchTerm, pageable);
            } else {
                return usuarioRepository.buscarUsuariosOptimizado(activo, searchTerm, empresaId, rolId, pageable);
            }
            
        } catch (Exception e) {
            System.err.println("Error en búsqueda de usuarios: " + e.getMessage());
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    // OPTIMIZADO: Métodos de conveniencia con caché
    @Cacheable(value = "usuariosActivosBusqueda", key = "#search + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Usuario> buscarUsuariosActivos(String search, Pageable pageable) {
        return buscarUsuarios(pageable, true, search, null, null);
    }

    @Cacheable(value = "usuariosInactivosBusqueda", key = "#search + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Usuario> buscarUsuariosInactivos(String search, Pageable pageable) {
        return buscarUsuarios(pageable, false, search, null, null);
    }

    public Page<Usuario> buscarUsuariosPorEmpresa(Long empresaId, String search, Pageable pageable) {
        return buscarUsuarios(pageable, null, search, empresaId, null);
    }

    public Page<Usuario> buscarUsuariosPorRol(Long rolId, String search, Pageable pageable) {
        return buscarUsuarios(pageable, null, search, null, rolId);
    }
    @Transactional
public void cambiarPassword(Long usuarioId, String currentPassword, String newPassword) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    // Verificar contraseña actual
    if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
        throw new SecurityException("Contraseña actual incorrecta");
    }
    
    // Cifrar y guardar nueva contraseña
    usuario.setPassword(passwordEncoder.encode(newPassword));
    usuarioRepository.save(usuario);
    
   
}
}

