package com.tickets.backend.security;

import com.tickets.backend.models.Permiso;
import com.tickets.backend.models.Rol;
import com.tickets.backend.models.Usuario;
import com.tickets.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario con email: " + email + " no existe en el sistema"));
    
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario está desactivado");
        }
    
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                getAuthorities(usuario)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    
    // Agregar roles como autoridades
    for (Rol rol : usuario.getRoles()) {
        // CORRECTO: No añadir ROLE_ porque ya viene en el nombre del rol
        authorities.add(new SimpleGrantedAuthority(rol.getNombre()));
        
        // Agregar permisos como autoridades
        for (Permiso permiso : rol.getPermisos()) {
            authorities.add(new SimpleGrantedAuthority(permiso.getNombre()));
        }
    }
    
    return authorities;
}
}