package com.tickets.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    
    private String apellido;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private String especialidad;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    private boolean activo;
    
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
    
    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;
    
    // NUEVA RELACIÓN CON CATEGORÍA
@ManyToOne
@JoinColumn(name = "categoria_id")
@JsonIgnoreProperties({"tickets", "subcategorias", "usuarios"}) // ⬅️ AGREGAR ESTO
private Categoria categoria;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();
    
    // Métodos para manejar roles
    public void addRol(Rol rol) {
        this.roles.add(rol);
    }
    
    public void removeRol(Rol rol) {
        this.roles.remove(rol);
    }
    
    // Método helper para verificar si es técnico
    public boolean esTecnico() {
        return this.roles.stream().anyMatch(rol -> rol.getNombre().equals("ROLE_TECNICO"));
    }
    
    // Método helper para verificar si es supervisor
    public boolean esSupervisor() {
        return this.roles.stream().anyMatch(rol -> rol.getNombre().equals("ROLE_SUPERVISOR"));
    }
}