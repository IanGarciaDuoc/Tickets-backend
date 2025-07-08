package com.tickets.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String nombre;
    
    private String descripcion;
    
    @Column(unique = true)
    private String rut;
    
    private boolean activo = true;
    
    // CORREGIDO: Usar @JsonIgnore para evitar lazy loading en contexto de usuarios
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore  // CRÍTICO: Evita serialización automática
    @ToString.Exclude  // Evita problemas con toString de Lombok
    private List<Sucursal> sucursales = new ArrayList<>();
    
    // Método para agregar sucursal
    public void addSucursal(Sucursal sucursal) {
        sucursales.add(sucursal);
        sucursal.setEmpresa(this);
    }
    
    // Método para eliminar sucursal
    public void removeSucursal(Sucursal sucursal) {
        sucursales.remove(sucursal);
        sucursal.setEmpresa(null);
    }
}