package com.tickets.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supervisor_tecnico", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"supervisor_id", "tecnico_id"})})
public class SupervisorTecnico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = false)
    @JsonIgnoreProperties({"password", "tickets", "historialCambios"})
    private Usuario supervisor;   // FALTABA ESTE ATRIBUTO
    
    @ManyToOne   // FALTABA ESTA ANOTACIÓN
    @JoinColumn(name = "tecnico_id", nullable = false)
    @JsonIgnoreProperties({"password", "tickets", "historialCambios"})
    private Usuario tecnico;
    
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
    
    private boolean activo = true;
    
    // Constructor para crear nueva relación
    public SupervisorTecnico(Usuario supervisor, Usuario tecnico) {
        this.supervisor = supervisor;
        this.tecnico = tecnico;
        this.fechaAsignacion = LocalDateTime.now();
        this.activo = true;
    }
}