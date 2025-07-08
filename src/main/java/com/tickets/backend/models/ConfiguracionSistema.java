package com.tickets.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionSistema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "clave", unique = true, nullable = false)
    private String clave;
    
    @Column(name = "valor", nullable = false)
    private String valor;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "tipo")
    private String tipo;
    
    // Constructor con campos principales
    public ConfiguracionSistema(String clave, String valor, String descripcion) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
        this.tipo = "SISTEMA"; // Valor por defecto
    }
    
    // Constructor con todos los campos excepto id
    public ConfiguracionSistema(String clave, String valor, String descripcion, String tipo) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }
}