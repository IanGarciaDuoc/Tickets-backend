package com.tickets.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String numeroTicket;
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    private EstadoTicket estado;
    
    @Enumerated(EnumType.STRING)
    private PrioridadTicket prioridad;
    
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    
    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;
    
    @ManyToOne
    @JoinColumn(name = "usuario_creador_id")
    private Usuario usuarioCreador;
    
    @ManyToOne
    @JoinColumn(name = "tecnico_asignado_id")
    private Usuario tecnicoAsignado;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
    
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    
    private Integer valoracion;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Adjunto> adjuntos = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialCambio> historialCambios = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "ticket_etiquetas",
        joinColumns = @JoinColumn(name = "ticket_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    private Set<Etiqueta> etiquetas = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "sla_id")
    private SLA sla;
    
    // Métodos auxiliares para las relaciones
    public void addComentario(Comentario comentario) {
        comentarios.add(comentario);
        comentario.setTicket(this);
    }
    
    public void removeComentario(Comentario comentario) {
        comentarios.remove(comentario);
        comentario.setTicket(null);
    }
    
    public void addAdjunto(Adjunto adjunto) {
        adjuntos.add(adjunto);
        adjunto.setTicket(this);
    }
    
    public void removeAdjunto(Adjunto adjunto) {
        adjuntos.remove(adjunto);
        adjunto.setTicket(null);
    }
    
    public void addEtiqueta(Etiqueta etiqueta) {
        etiquetas.add(etiqueta);
    }
    
    public void removeEtiqueta(Etiqueta etiqueta) {
        etiquetas.remove(etiqueta);
    }
    
    public void addHistorialCambio(HistorialCambio cambio) {
        historialCambios.add(cambio);
        cambio.setTicket(this);
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }
    
    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }
}