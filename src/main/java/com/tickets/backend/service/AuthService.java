package com.tickets.backend.service;

import com.tickets.backend.dto.JwtAuthResponse;
import com.tickets.backend.dto.LoginRequest;
import com.tickets.backend.dto.RegistroRequest;
import com.tickets.backend.models.Empresa;
import com.tickets.backend.models.Rol;
import com.tickets.backend.models.Sucursal;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.RolRepository;
import com.tickets.backend.repository.SucursalRepository;
import com.tickets.backend.repository.EmpresaRepository;
import com.tickets.backend.repository.UsuarioRepository;
import com.tickets.backend.security.JwtTokenProvider;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;
    
    public JwtAuthResponse autenticar(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toList());

        // Modificado para incluir nombre y apellido
        return new JwtAuthResponse(
            jwt, 
            "Bearer", 
            usuario.getId(), 
            usuario.getEmail(), 
            usuario.getNombre(), 
            usuario.getApellido(), 
            roles
        );
    }

    @Transactional
    public Usuario registrarUsuario(RegistroRequest registroRequest) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
            throw new RuntimeException("Email ya está en uso");
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(registroRequest.getNombre());
        usuario.setApellido(registroRequest.getApellido());
        usuario.setEmail(registroRequest.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroRequest.getPassword()));
        usuario.setEspecialidad(registroRequest.getEspecialidad());
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setActivo(true);
        
        // Asignar empresa si se proporciona un ID
        if (registroRequest.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(registroRequest.getEmpresaId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            usuario.setEmpresa(empresa);
        }
        
        // Asignar sucursal si se proporciona un ID
        if (registroRequest.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(registroRequest.getSucursalId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
            
            // Verificar que la sucursal pertenece a la empresa
            if (usuario.getEmpresa() != null && !sucursal.getEmpresa().getId().equals(usuario.getEmpresa().getId())) {
                throw new RuntimeException("La sucursal no pertenece a la empresa seleccionada");
            }
            
            usuario.setSucursal(sucursal);
        }

        // Asignar rol de usuario por defecto
        Set<Rol> roles = new HashSet<>();
        Rol rolUsuario = rolRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        roles.add(rolUsuario);
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }
}