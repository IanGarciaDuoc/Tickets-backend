// src/main/java/com/tickets/backend/controller/UsuarioController.java
package com.tickets.backend.controller;

import com.tickets.backend.dto.UsuarioDto;
import com.tickets.backend.models.Rol;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.service.UsuarioService;
import com.tickets.backend.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // OPTIMIZADO: Usa el método optimizado del servicio
    @GetMapping
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<Map<String, Object>> obtenerTodosUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        // OPTIMIZADO: Ya usa el método optimizado en el servicio
        Page<Usuario> pageUsuarios = usuarioService.obtenerTodosUsuarios(pageable);
        
        // OPTIMIZADO: Conversión eficiente con stream paralelo para listas grandes
        List<UsuarioDto> usuariosDto;
        if (pageUsuarios.getContent().size() > 50) {
            usuariosDto = pageUsuarios.getContent().parallelStream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } else {
            usuariosDto = pageUsuarios.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", usuariosDto);
        response.put("currentPage", pageUsuarios.getNumber());
        response.put("totalItems", pageUsuarios.getTotalElements());
        response.put("totalPages", pageUsuarios.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    // OPTIMIZADO: Endpoint de búsqueda principal
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<Map<String, Object>> buscarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long rolId) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            // OPTIMIZADO: Ya usa métodos optimizados en el servicio
            Page<Usuario> pageUsuarios = usuarioService.buscarUsuarios(pageable, activo, search, empresaId, rolId);
            
            // OPTIMIZADO: Conversión eficiente
            List<UsuarioDto> usuariosDto;
            if (pageUsuarios.getContent().size() > 50) {
                usuariosDto = pageUsuarios.getContent().parallelStream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            } else {
                usuariosDto = pageUsuarios.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", usuariosDto);
            response.put("content", usuariosDto);
            response.put("currentPage", pageUsuarios.getNumber());
            response.put("totalItems", pageUsuarios.getTotalElements());
            response.put("totalElements", pageUsuarios.getTotalElements());
            response.put("totalPages", pageUsuarios.getTotalPages());
            response.put("size", pageUsuarios.getSize());
            response.put("numberOfElements", pageUsuarios.getNumberOfElements());
            response.put("first", pageUsuarios.isFirst());
            response.put("last", pageUsuarios.isLast());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error en búsqueda de usuarios: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", List.of());
            errorResponse.put("totalItems", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("error", "Error interno del servidor");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<Map<String, Object>> buscarUsuariosAlternativo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long rolId) {
        
        return buscarUsuarios(page, size, activo, search, empresaId, rolId);
    }

    // CORREGIDO: Método de conversión más eficiente y seguro
    private UsuarioDto convertToDto(Usuario usuario) {
        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setEspecialidad(usuario.getEspecialidad());
        dto.setActivo(usuario.isActivo());
        
        // CORREGIDO: Acceso seguro a empresa sin cargar sucursales
        if (usuario.getEmpresa() != null) {
            dto.setEmpresaId(usuario.getEmpresa().getId());
            // NO acceder a sucursales aquí para evitar lazy loading
        }
        
        if (usuario.getSucursal() != null) {
            dto.setSucursalId(usuario.getSucursal().getId());
        }
        
        if (usuario.getCategoria() != null) {
            dto.setCategoriaId(usuario.getCategoria().getId());
        }
        
        // CORREGIDO: Acceso seguro a roles
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            try {
                dto.setRolesId(usuario.getRoles().stream()
                    .map(Rol::getId)
                    .collect(Collectors.toSet()));
                
                dto.setRoles(usuario.getRoles().stream()
                    .map(Rol::getNombre)
                    .collect(Collectors.toList()));
            } catch (Exception e) {
                // En caso de lazy loading error, establecer valores por defecto
                dto.setRolesId(Set.of());
                dto.setRoles(List.of());
            }
        }
        
        return dto;
    }

    // OPTIMIZADO: Usa métodos optimizados del servicio
    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosActivos() {
        // OPTIMIZADO: Ya usa caché en el servicio
        List<Usuario> usuarios = usuarioService.obtenerUsuariosActivos();
        List<UsuarioDto> usuariosDto = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDto);
    }

    @GetMapping("/inactivos")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosInactivos() {
        // OPTIMIZADO: Ya usa caché en el servicio
        List<Usuario> usuarios = usuarioService.obtenerUsuariosInactivos();
        List<UsuarioDto> usuariosDto = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<UsuarioDto> obtenerUsuarioPorId(@PathVariable Long id) {
        // OPTIMIZADO: Ya usa consulta optimizada en el servicio
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(convertToDto(usuario));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREAR_USUARIOS')")
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        return new ResponseEntity<>(usuarioService.crearUsuario(usuarioDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTUALIZAR_USUARIOS')")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDto usuarioDto) {
        
        try {
            // OPTIMIZADO: El servicio ya maneja todo de manera optimizada
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuarioDto);
            
            // Si vienen roles adicionales, asignarlos
            if (usuarioDto.getRolesId() != null && !usuarioDto.getRolesId().isEmpty()) {
                usuarioActualizado = usuarioService.asignarRolesAUsuario(id, usuarioDto.getRolesId());
            }
            else if (usuarioDto.getRoles() != null && !usuarioDto.getRoles().isEmpty()) {
                Set<Long> rolesIds = convertRoleNamesToIds(usuarioDto.getRoles());
                if (!rolesIds.isEmpty()) {
                    usuarioActualizado = usuarioService.asignarRolesAUsuario(id, rolesIds);
                }
            }
            
            return ResponseEntity.ok(usuarioActualizado);
            
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Set<Long> convertRoleNamesToIds(List<String> roleNames) {
        Map<String, Long> roleMapping = new HashMap<>();
        roleMapping.put("ROLE_ADMIN", 6L);
        roleMapping.put("ROLE_SUPERVISOR", 7L);
        roleMapping.put("ROLE_TECNICO", 8L);
        roleMapping.put("ROLE_USER", 9L);
        
        return roleNames.stream()
            .map(roleMapping::get)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('ACTUALIZAR_USUARIOS')")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ELIMINAR_USUARIOS')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ACTUALIZAR_USUARIOS')")
    public ResponseEntity<Void> activarUsuario(@PathVariable Long id) {
        usuarioService.activarUsuario(id);
        return ResponseEntity.ok().build();
    }
    
    // OPTIMIZADO: Usar método optimizado del servicio
    @GetMapping("/tecnicos")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerTecnicos() {
        // OPTIMIZADO: Ya usa consulta optimizada y caché en el servicio
        List<Usuario> usuarios = usuarioService.obtenerTecnicosActivos();
        List<UsuarioDto> usuariosDto = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDto);
    }
    
    @GetMapping("/tecnicos/especialidad/{especialidad}")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerTecnicosPorEspecialidad(@PathVariable String especialidad) {
        List<Usuario> tecnicos = usuarioService.obtenerTecnicosPorEspecialidad(especialidad);
        List<UsuarioDto> tecnicosDto = tecnicos.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tecnicosDto);
    }

    @PostMapping("/{usuarioId}/roles")
    @PreAuthorize("hasAuthority('ACTUALIZAR_USUARIOS')")
    public ResponseEntity<Usuario> asignarRolesAUsuario(
            @PathVariable Long usuarioId,
            @RequestBody Set<Long> rolesIds) {
        return ResponseEntity.ok(usuarioService.asignarRolesAUsuario(usuarioId, rolesIds));
    }

    // OPTIMIZADO: Usar método optimizado del servicio
    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuariosPorCategoria(@PathVariable Long categoriaId) {
        // OPTIMIZADO: Usar consulta optimizada del repositorio
        List<Usuario> usuarios = usuarioRepository.findByCategoriaIdAndActivoTrue(categoriaId);
        List<UsuarioDto> usuariosDto = usuarios.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDto);
    }

    // OPTIMIZADO: Usar método optimizado del servicio
    @GetMapping("/supervisores")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerSupervisores() {
        // OPTIMIZADO: Usar consulta optimizada
        List<Usuario> supervisores = usuarioRepository.findByRolesNombreAndActivoTrueOptimized("ROLE_SUPERVISOR");
        List<UsuarioDto> supervisoresDto = supervisores.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(supervisoresDto);
    }

    // OPTIMIZADO: Usar método optimizado del servicio
    @GetMapping("/supervisores/categoria/{categoriaId}")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerSupervisoresPorCategoria(@PathVariable Long categoriaId) {
        // OPTIMIZADO: Ya usa caché en el servicio
        List<Usuario> supervisores = usuarioService.obtenerSupervisoresPorCategoria(categoriaId);
        List<UsuarioDto> supervisoresDto = supervisores.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(supervisoresDto);
    }

    // OPTIMIZADO: Usar método optimizado del servicio
    @GetMapping("/tecnicos/categoria/{categoriaId}")
    @PreAuthorize("hasAuthority('LEER_USUARIOS')")
    public ResponseEntity<List<UsuarioDto>> obtenerTecnicosPorCategoria(@PathVariable Long categoriaId) {
        try {
            // OPTIMIZADO: Ya usa consulta optimizada y caché en el servicio
            List<Usuario> tecnicos = usuarioService.obtenerTecnicosPorCategoria(categoriaId);
            
            List<UsuarioDto> tecnicosDto = tecnicos.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(tecnicosDto);
        } catch (Exception e) {
            System.err.println("Error al obtener técnicos por categoría: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
@PutMapping("/{id}/change-password")
@PreAuthorize("hasAuthority('ACTUALIZAR_USUARIOS')")
public ResponseEntity<Map<String, String>> cambiarPassword(
        @PathVariable Long id,
        @RequestBody Map<String, String> passwordData) {
    
    try {
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        // Validaciones
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Contraseña actual y nueva son obligatorias"));
        }
        
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres"));
        }
        
        // Llamar al servicio para cambiar contraseña
        usuarioService.cambiarPassword(id, currentPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
        
    } catch (SecurityException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Contraseña actual incorrecta"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Error interno del servidor"));
    }
}
}