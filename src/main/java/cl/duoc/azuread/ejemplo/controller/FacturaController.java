package cl.duoc.azuread.ejemplo.controller;

import cl.duoc.azuread.ejemplo.model.Factura;
import cl.duoc.azuread.ejemplo.service.FacturaService;
import cl.duoc.azuread.ejemplo.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;
    private final S3Service s3Service;

    public FacturaController(FacturaService facturaService, S3Service s3Service) {
        this.facturaService = facturaService;
        this.s3Service = s3Service;
    }

    // Crear factura - solo usuarios con ROLE_GESTION
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_GESTION')")
    public ResponseEntity<Factura> crear(@RequestBody Factura factura) {
        return ResponseEntity.ok(facturaService.crearFactura(factura));
    }

    // Actualizar factura - solo usuarios con ROLE_GESTION
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_GESTION')")
    public ResponseEntity<Factura> actualizar(@PathVariable Long id, @RequestBody Factura factura) {
        return ResponseEntity.ok(facturaService.actualizarFactura(id, factura));
    }

    // Eliminar factura - solo usuarios con ROLE_GESTION
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_GESTION')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }

    // Consultar historial por cliente - solo usuarios con ROLE_GESTION
    @GetMapping("/cliente/{cliente}")
    @PreAuthorize("hasAuthority('ROLE_GESTION')")
    public ResponseEntity<List<Factura>> historial(@PathVariable String cliente) {
        return ResponseEntity.ok(facturaService.obtenerHistorialPorCliente(cliente));
    }

    // Descargar factura - solo usuarios con ROLE_DESCARGA
    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAuthority('ROLE_DESCARGA')")
    public ResponseEntity<String> descargar(@PathVariable Long id) {
        return facturaService.obtenerFacturaPorId(id)
                .map(factura -> ResponseEntity.ok(factura.getUrlS3()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Subir archivo a S3 y vincularlo a la factura - solo usuarios con ROLE_GESTION
    @PostMapping("/{id}/subir")
    @PreAuthorize("hasAuthority('ROLE_GESTION')")
    public ResponseEntity<String> subirArchivo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            String url = s3Service.subirArchivo(file);
            return facturaService.obtenerFacturaPorId(id).map(factura -> {
                factura.setUrlS3(url);
                facturaService.crearFactura(factura);
                return ResponseEntity.ok(url);
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir archivo: " + e.getMessage());
        }
    }
}
