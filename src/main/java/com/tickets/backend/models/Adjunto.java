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
@Table(name = "adjuntos")
public class Adjunto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @Column(name = "nombre_archivo")
    private String nombreArchivo;
    
    @Column(name = "ruta_archivo")
    private String rutaArchivo;
    
    @Column(name = "tipo_archivo")
    private String tipoArchivo;
    
    private Long tamaño;
    
    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;
}