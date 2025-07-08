package com.tickets.backend.controller;

import com.tickets.backend.dto.EmpresaDto;
import com.tickets.backend.dto.SucursalDto;
import com.tickets.backend.models.Empresa;
import com.tickets.backend.models.Sucursal;
import com.tickets.backend.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<List<Empresa>> obtenerTodasEmpresas() {
        return ResponseEntity.ok(empresaService.obtenerTodasEmpresas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Empresa>> obtenerEmpresasActivas() {
        return ResponseEntity.ok(empresaService.obtenerEmpresasActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtenerEmpresaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaService.obtenerEmpresaPorId(id));
    }

    @PostMapping
    public ResponseEntity<Empresa> crearEmpresa(@Valid @RequestBody EmpresaDto empresaDto) {
        return new ResponseEntity<>(empresaService.crearEmpresa(empresaDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizarEmpresa(
            @PathVariable Long id,
            @Valid @RequestBody EmpresaDto empresaDto) {
        return ResponseEntity.ok(empresaService.actualizarEmpresa(id, empresaDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpresa(@PathVariable Long id) {
        empresaService.eliminarEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints para sucursales
    @GetMapping("/{empresaId}/sucursales")
    public ResponseEntity<List<Sucursal>> obtenerSucursalesPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(empresaService.obtenerSucursalesPorEmpresa(empresaId));
    }

    @PostMapping("/{empresaId}/sucursales")
    public ResponseEntity<Sucursal> crearSucursal(
            @PathVariable Long empresaId,
            @Valid @RequestBody SucursalDto sucursalDto) {
        return new ResponseEntity<>(empresaService.crearSucursal(empresaId, sucursalDto), HttpStatus.CREATED);
    }

    @PutMapping("/sucursales/{id}")
    public ResponseEntity<Sucursal> actualizarSucursal(
            @PathVariable Long id,
            @Valid @RequestBody SucursalDto sucursalDto) {
        return ResponseEntity.ok(empresaService.actualizarSucursal(id, sucursalDto));
    }

    @DeleteMapping("/sucursales/{id}")
    public ResponseEntity<Void> eliminarSucursal(@PathVariable Long id) {
        empresaService.eliminarSucursal(id);
        return ResponseEntity.noContent().build();
    }
}