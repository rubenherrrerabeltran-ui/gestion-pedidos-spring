package co.sena.gestionpedidos.web.controlador;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.ReporteServicio;
import co.sena.gestionpedidos.util.Validador;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;

/**
 * Módulo web de reportes de ventas (versión 1.3.0) para el administrador y el vendedor.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Controller
@RequestMapping("/reportes")
@RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
public class ReporteControlador {

    /** Marca de orden de bytes UTF-8: hace que Excel muestre bien las tildes del CSV. */
    private static final String BOM_UTF8 = "﻿";

    private final ReporteServicio reporteServicio;

    /**
     * @param reporteServicio servicio que integra pedidos, productos y clientes
     */
    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    /**
     * GET /reportes?desde=&amp;hasta=: muestra el reporte de ventas del periodo.
     *
     * @param desde  fecha inicial en formato AAAA-MM-DD (opcional)
     * @param hasta  fecha final en formato AAAA-MM-DD (opcional)
     * @param modelo modelo para la vista
     * @return plantilla {@code reportes/ventas}
     */
    @GetMapping
    public String ventas(@RequestParam(required = false) String desde,
                         @RequestParam(required = false) String hasta, Model modelo) {
        modelo.addAttribute("hoy", LocalDate.now());
        try {
            modelo.addAttribute("reporte", reporteServicio.generar(Validador.convertirFecha(desde, "inicial"),
                    Validador.convertirFecha(hasta, "final")));
        } catch (NegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("reporte", reporteServicio.generar(null, null));
        }
        return "reportes/ventas";
    }

    /**
     * GET /reportes/pedidos.csv: descarga los pedidos del periodo en un archivo CSV.
     *
     * @param desde fecha inicial (opcional)
     * @param hasta fecha final (opcional)
     * @return archivo CSV como adjunto
     */
    @GetMapping("/pedidos.csv")
    public ResponseEntity<byte[]> exportar(@RequestParam(required = false) String desde,
                                           @RequestParam(required = false) String hasta) {
        String csv = reporteServicio.exportarCsv(Validador.convertirFecha(desde, "inicial"),
                Validador.convertirFecha(hasta, "final"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("pedidos.csv").build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body((BOM_UTF8 + csv).getBytes(StandardCharsets.UTF_8));
    }
}
