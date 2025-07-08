// NotificacionCategoria.java
package com.tickets.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificaciones_categoria")
public class NotificacionCategoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    
    @Column(nullable = false)
    private String email;
    
    private String descripcion;
    
    private boolean activo = true;
}

    // Getters and Setters (si no usas Lombok)
    // public Long getId() { return id; }
    // public void setId(Long id) { this.id = id; }
    // public Categoria getCategoria() { return categoria; }
    // public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    // public String getEmail() { return email; }
    // public void setEmail(String email) { this.email = email; }
    // public String getDescripcion() { return descripcion; }
    // public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    // public boolean isActivo() { return activo; }
    // public void setActivo(boolean activo) { this.activo = activo; }