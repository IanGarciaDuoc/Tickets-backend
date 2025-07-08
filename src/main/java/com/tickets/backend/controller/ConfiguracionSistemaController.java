package com.tickets.backend.controller;

import com.tickets.backend.models.ConfiguracionSistema;
import com.tickets.backend.service.ConfiguracionSistemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/configuraciones")
public class ConfiguracionSistemaController {

    @Autowired
    private ConfiguracionSistemaService configuracionService;
    
    /**
     * Obtener todas las configuraciones
     */
    @GetMapping
    public ResponseEntity<List<ConfiguracionSistema>> obtenerTodasLasConfiguraciones() {
        return ResponseEntity.ok(configuracionService.obtenerTodas());
    }
    
    /**
     * Obtener configuración por clave
     */
    @GetMapping("/clave/{clave}")
    public ResponseEntity<ConfiguracionSistema> obtenerPorClave(@PathVariable String clave) {
        return configuracionService.obtenerPorClave(clave)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Crear una nueva configuración
     */
    @PostMapping
    public ResponseEntity<ConfiguracionSistema> crearConfiguracion(@RequestBody ConfiguracionSistema configuracion) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(configuracionService.guardar(configuracion));
    }
    
    /**
     * Guardar configuraciones en lote
     */
    @PostMapping("/batch")
    public ResponseEntity<Boolean> guardarConfiguracionesLote(@RequestBody List<ConfiguracionSistema> configuraciones) {
        try {
            configuracionService.guardarTodas(configuraciones);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    /**
     * Endpoint específico para actualizar múltiples configuraciones 
     * (adaptado para la configuración de tickets)
     */
    @PostMapping("/actualizar-multiple")
    public ResponseEntity<Boolean> actualizarMultipleConfiguraciones(
            @RequestBody List<ConfiguracionSistema> configuraciones) {
        try {
            // Utilizamos el método existente en el servicio
            configuracionService.guardarTodas(configuraciones);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace(); // Registrar el error para diagnóstico
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    
    /**
     * Actualizar una configuración existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracionSistema> actualizarConfiguracion(
            @PathVariable Long id, 
            @RequestBody ConfiguracionSistema configuracion) {
        
        return configuracionService.obtenerPorId(id)
            .map(configExistente -> {
                configuracion.setId(id);
                return ResponseEntity.ok(configuracionService.guardar(configuracion));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Actualizar valor de una configuración por clave
     */
    @PatchMapping("/clave/{clave}")
    public ResponseEntity<ConfiguracionSistema> actualizarValorPorClave(
            @PathVariable String clave,
            @RequestBody Map<String, String> datos) {
        
        String nuevoValor = datos.get("valor");
        if (nuevoValor == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return configuracionService.obtenerPorClave(clave)
            .map(configExistente -> {
                configExistente.setValor(nuevoValor);
                return ResponseEntity.ok(configuracionService.guardar(configExistente));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Eliminar una configuración
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarConfiguracion(@PathVariable Long id) {
        return configuracionService.obtenerPorId(id)
            .map(configExistente -> {
                configuracionService.eliminar(id);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtener configuraciones por tipo (para agrupar configuraciones)
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ConfiguracionSistema>> obtenerPorTipo(@PathVariable String tipo) {
        List<ConfiguracionSistema> configuraciones = configuracionService.obtenerPorTipo(tipo);
        return ResponseEntity.ok(configuraciones);
    }
    
    /**
     * Obtener configuraciones específicas para tickets
     */
   @GetMapping("/configuracion/tickets")
public ResponseEntity<List<ConfiguracionSistema>> obtenerConfiguracionTickets() {
    List<String> claves = List.of(
        "DIAS_PARA_CIERRE_AUTOMATICO", 
        "CIERRE_AUTOMATICO_HABILITADO",
        "PREFIJO_NUMERO_TICKET",
        "DIGITOS_NUMERO_TICKET",
        "FRECUENCIA_CIERRE_AUTOMATICO",
        "HORARIO_CIERRE_AUTOMATICO", 
        "MINUTOS_CIERRE_AUTOMATICO"
    );
    
    List<ConfiguracionSistema> configuraciones = configuracionService.obtenerPorClaves(claves);
    return ResponseEntity.ok(configuraciones);

}
}