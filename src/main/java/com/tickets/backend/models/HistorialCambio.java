package com.tickets.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "historial_cambios")
public class HistorialCambio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @Column(name = "campo_modificado")
    private String campoModificado;
    
    @Column(name = "valor_anterior")
    private String valorAnterior;
    
    @Column(name = "valor_nuevo")
    private String valorNuevo;
    
    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;
    
    // Nuevo campo para indicar si el cambio fue automático
    @Column(name = "automatico")
    private boolean automatico = false;
}