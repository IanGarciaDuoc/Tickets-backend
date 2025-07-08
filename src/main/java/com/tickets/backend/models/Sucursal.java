package com.tickets.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sucursales")
public class Sucursal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    private String nombre;
    
    private String telefono;
    
    private boolean activo = true;
    
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    @NotNull(message = "La sucursal debe pertenecer a una empresa")
    @JsonBackReference
    private Empresa empresa;
    
    // Relación con Dirección
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;
}