package co.sena.gestionpedidos.movil.datos.remoto;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.sena.gestionpedidos.movil.datos.json.Json;
import co.sena.gestionpedidos.movil.datos.mapeo.Mapeador;
import co.sena.gestionpedidos.movil.dominio.carrito.Carrito;
import co.sena.gestionpedidos.movil.dominio.carrito.ItemCarrito;
import co.sena.gestionpedidos.movil.dominio.modelo.EstadoPedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.dominio.modelo.Producto;
import co.sena.gestionpedidos.movil.dominio.modelo.ReporteVentas;
import co.sena.gestionpedidos.movil.dominio.modelo.Resumen;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.util.Validador;

/**
 * Fachada (patrón Facade/Repository) de la API REST del Sistema de Gestión de Pedidos.
 * Cada método corresponde a un ENDPOINT del backend Spring Boot y devuelve objetos del dominio.
 * Los métodos son bloqueantes: la interfaz los llama en un hilo de fondo.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ApiGestionPedidos {

    private final TransporteHttp transporte;
    private final String urlBase;

    /**
     * @param transporte estrategia de transporte HTTP
     * @param urlBase    dirección del servidor, por ejemplo http://10.0.2.2:8080
     */
    public ApiGestionPedidos(TransporteHttp transporte, String urlBase) {
        this.transporte = transporte;
        this.urlBase = Validador.normalizarUrl(urlBase);
    }

    public String getUrlBase() {
        return urlBase;
    }

    /** POST /api/sesion */
    public Usuario iniciarSesion(String correo, String clave) throws ApiException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("correo", correo.trim());
        cuerpo.put("clave", clave);
        return Mapeador.usuario(Json.leerObjeto(enviar("POST", "/api/sesion", Json.escribir(cuerpo))));
    }

    /** DELETE /api/sesion (y olvida la cookie aunque falle la red). */
    public void cerrarSesion() {
        try {
            enviar("DELETE", "/api/sesion", null);
        } catch (ApiException ignorada) {
            // La sesión local se cierra de todas formas.
        } finally {
            transporte.limpiarSesion();
        }
    }

    /** GET /api/productos?texto= */
    public List<Producto> listarProductos(String texto) throws ApiException {
        String ruta = "/api/productos";
        if (!Validador.estaVacio(texto)) {
            ruta += "?texto=" + codificar(texto.trim());
        }
        return Mapeador.productos(Json.leerLista(enviar("GET", ruta, null)));
    }

    /** GET /api/pedidos?estado= (el cliente solo recibe sus pedidos) */
    public List<Pedido> listarPedidos(EstadoPedido estado) throws ApiException {
        String ruta = "/api/pedidos" + (estado == null ? "" : "?estado=" + estado.name());
        return Mapeador.pedidos(Json.leerLista(enviar("GET", ruta, null)));
    }

    /** GET /api/pedidos/{id} */
    public Pedido obtenerPedido(int idPedido) throws ApiException {
        return Mapeador.pedido(Json.leerObjeto(enviar("GET", "/api/pedidos/" + idPedido, null)));
    }

    /**
     * POST /api/pedidos con los productos del carrito.
     *
     * @param idCliente cliente del pedido (el servidor usa el de la sesión si es CLIENTE)
     * @param carrito   productos y cantidades
     * @return pedido registrado con sus totales
     */
    public Pedido crearPedido(int idCliente, Carrito carrito) throws ApiException {
        List<Object> items = new ArrayList<>();
        for (ItemCarrito item : carrito.getItems()) {
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("idProducto", item.getProducto().getIdProducto());
            linea.put("cantidad", item.getCantidad());
            items.add(linea);
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("idCliente", idCliente);
        cuerpo.put("items", items);
        return Mapeador.pedido(Json.leerObjeto(enviar("POST", "/api/pedidos", Json.escribir(cuerpo))));
    }

    /** PATCH /api/pedidos/{id}/estado */
    public Pedido cambiarEstado(int idPedido, EstadoPedido estado) throws ApiException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", estado.name());
        return Mapeador.pedido(Json.leerObjeto(
                enviar("PATCH", "/api/pedidos/" + idPedido + "/estado", Json.escribir(cuerpo))));
    }

    /** GET /api/resumen (ADMINISTRADOR y VENDEDOR) */
    public Resumen obtenerResumen() throws ApiException {
        return Mapeador.resumen(Json.leerObjeto(enviar("GET", "/api/resumen", null)));
    }

    /** GET /api/reportes/ventas?desde=&hasta= (ADMINISTRADOR y VENDEDOR) */
    public ReporteVentas obtenerReporte(String desde, String hasta) throws ApiException {
        StringBuilder ruta = new StringBuilder("/api/reportes/ventas");
        char separador = '?';
        if (!Validador.estaVacio(desde)) {
            ruta.append(separador).append("desde=").append(codificar(desde.trim()));
            separador = '&';
        }
        if (!Validador.estaVacio(hasta)) {
            ruta.append(separador).append("hasta=").append(codificar(hasta.trim()));
        }
        return Mapeador.reporte(Json.leerObjeto(enviar("GET", ruta.toString(), null)));
    }

    private String enviar(String metodo, String ruta, String cuerpo) throws ApiException {
        RespuestaHttp respuesta;
        try {
            respuesta = transporte.enviar(metodo, urlBase + ruta, cuerpo);
        } catch (IOException e) {
            throw new ApiException(ApiException.SIN_CONEXION,
                    "No hay conexión con el servidor " + urlBase + ". Verifique que la API esté iniciada.");
        }
        if (!respuesta.esExitosa()) {
            throw new ApiException(respuesta.getEstado(), mensajeDeError(respuesta));
        }
        return respuesta.getCuerpo();
    }

    private static String mensajeDeError(RespuestaHttp respuesta) {
        if (respuesta.getEstado() == 401) {
            return "Su sesión no está activa. Inicie sesión de nuevo.";
        }
        try {
            Object detalle = Json.leerObjeto(respuesta.getCuerpo()).get("detail");
            if (detalle != null) {
                return detalle.toString();
            }
        } catch (IllegalArgumentException ignorada) {
            // El cuerpo no es un ProblemDetail: se usa el mensaje genérico.
        }
        return "El servidor respondió con el código " + respuesta.getEstado();
    }

    private static String codificar(String valor) {
        try {
            return URLEncoder.encode(valor, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
