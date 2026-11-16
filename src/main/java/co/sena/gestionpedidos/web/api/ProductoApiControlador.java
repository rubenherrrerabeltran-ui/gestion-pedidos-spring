package co.sena.gestionpedidos.web.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.sena.gestionpedidos.modelo.Producto;
import co.sena.gestionpedidos.modelo.Rol;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.web.api.dto.ProductoSolicitud;
import co.sena.gestionpedidos.web.seguridad.Publico;
import co.sena.gestionpedidos.web.seguridad.RequiereRol;

/**
 * API REST de productos (JSON). Usa los verbos HTTP según su propósito:
 * GET consulta, POST crea, PUT actualiza y DELETE elimina.
 * La consulta del catálogo es pública; las modificaciones exigen rol ADMINISTRADOR o VENDEDOR.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoApiControlador {

    private final ProductoServicio productoServicio;

    /**
     * @param productoServicio servicio de productos (la misma lógica que usan las vistas web)
     */
    public ProductoApiControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    /**
     * GET /api/productos?texto=...&amp;filtro=todos|activos|inactivos|stock-bajo
     *
     * @param texto  filtro opcional por nombre
     * @param filtro estado del inventario (por defecto todos)
     * @return lista de productos en JSON
     */
    @GetMapping
    @Publico
    public List<Producto> listar(@RequestParam(required = false) String texto,
                                 @RequestParam(required = false, defaultValue = "todos") String filtro) {
        return productoServicio.listar(texto, filtro);
    }

    /**
     * GET /api/productos/{id}
     *
     * @param id identificador
     * @return el producto (404 si no existe)
     */
    @GetMapping("/{id}")
    @Publico
    public Producto obtener(@PathVariable int id) {
        return productoServicio.obtener(id);
    }

    /**
     * POST /api/productos
     *
     * @param solicitud datos del producto en JSON
     * @return 201 Created con la cabecera Location y el producto creado
     */
    @PostMapping
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public ResponseEntity<Producto> crear(@RequestBody ProductoSolicitud solicitud) {
        Producto creado = productoServicio.guardar(solicitud.aProducto(null));
        return ResponseEntity.created(URI.create("/api/productos/" + creado.getIdProducto())).body(creado);
    }

    /**
     * PUT /api/productos/{id}
     *
     * @param id        producto a actualizar
     * @param solicitud nuevos datos
     * @return el producto actualizado
     */
    @PutMapping("/{id}")
    @RequiereRol({Rol.ADMINISTRADOR, Rol.VENDEDOR})
    public Producto actualizar(@PathVariable int id, @RequestBody ProductoSolicitud solicitud) {
        return productoServicio.guardar(solicitud.aProducto(id));
    }

    /**
     * DELETE /api/productos/{id}
     *
     * @param id producto a eliminar
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @RequiereRol(Rol.ADMINISTRADOR)
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        productoServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
