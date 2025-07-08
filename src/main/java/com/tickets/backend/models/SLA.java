package com.tickets.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "slas")
public class SLA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    private PrioridadTicket prioridad;
    
    @Column(name = "tiempo_respuesta")
    private Integer tiempoRespuesta; // en minutos
    
    @Column(name = "tiempo_resolucion")
    private Integer tiempoResolucion; // en minutos
    
    private boolean activo = true;
}