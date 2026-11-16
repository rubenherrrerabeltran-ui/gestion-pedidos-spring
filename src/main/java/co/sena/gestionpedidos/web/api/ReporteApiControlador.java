package co.sena.gestionpedidos.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.ReporteServicio;
import co.sena.gestionpedidos.servicio.reporte.ReporteVentas;
import co.sena.gestionpedidos.util.Validador;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;

/**
 * Servicio web del módulo de reportes (versión 1.3.0).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/reportes")
@RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
public class ReporteApiControlador {

    private final ReporteServicio reporteServicio;

    /**
     * @param reporteServicio servicio de reportes
     */
    public ReporteApiControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    /**
     * GET /api/reportes/ventas?desde=&amp;hasta=
     *
     * @param desde fecha inicial AAAA-MM-DD (opcional)
     * @param hasta fecha final AAAA-MM-DD (opcional)
     * @return reporte de ventas del periodo en JSON
     */
    @GetMapping("/ventas")
    public ReporteVentas ventas(@RequestParam(required = false) String desde,
                                @RequestParam(required = false) String hasta) {
        return reporteServicio.generar(Validador.convertirFecha(desde, "inicial"),
                Validador.convertirFecha(hasta, "final"));
    }
}
