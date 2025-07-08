package com.tickets.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "etiquetas")
public class Etiqueta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String nombre;
    
    private String color;
    
    private String descripcion;
    
    @JsonIgnore  // ← AGREGAR ESTA ANOTACIÓN
    @ManyToMany(mappedBy = "etiquetas")
    private Set<Ticket> tickets = new HashSet<>();
}