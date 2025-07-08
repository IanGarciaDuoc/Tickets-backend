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

/**
 * Entidad para almacenar configuraciones de servidor de correo
 */
@Entity
@Table(name = "configuracion_correo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionCorreo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "host", nullable = false)
    private String host;
    
    @Column(name = "puerto", nullable = false)
    private Integer puerto;
    
    @Column(name = "usuario", nullable = false)
    private String usuario;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "ssl_habilitado")
    private Boolean sslHabilitado = false;
    
    @Column(name = "tls_habilitado")
    private Boolean tlsHabilitado = true;
    
    @Column(name = "auth_requerida")
    private Boolean authRequerida = true;
    
    @Column(name = "remitente", nullable = false)
    private String remitente;
    
    @Column(name = "nombre_remitente")
    private String nombreRemitente;
    
    @Column(name = "activo")
    private Boolean activo = true;
}