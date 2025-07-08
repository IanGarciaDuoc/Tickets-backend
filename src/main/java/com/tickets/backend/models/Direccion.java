package com.tickets.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "direcciones")
public class Direccion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String calle;
    
    private String numero;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id")
    @JsonBackReference(value = "comuna-direccion")
    private Comuna comuna;
    
    @OneToOne(mappedBy = "direccion")
    @JsonBackReference(value = "sucursal-direccion")
    private Sucursal sucursal;
    
    private boolean activo = true;


    
}