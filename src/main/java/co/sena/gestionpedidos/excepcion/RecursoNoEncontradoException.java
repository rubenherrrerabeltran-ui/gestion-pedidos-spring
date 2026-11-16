package co.sena.gestionpedidos.excepcion;

/**
 * Se lanza cuando se consulta un registro (producto, usuario o pedido) que no existe.
 * Los manejadores de errores la convierten en una respuesta HTTP 404.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param recurso nombre del recurso buscado, por ejemplo "Producto"
     * @param id      identificador buscado
     */
    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id " + id + " no existe");
    }
}
