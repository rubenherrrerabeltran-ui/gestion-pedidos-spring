package co.sena.gestionpedidos.web.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import co.sena.gestionpedidos.excepcion.AccesoDenegadoException;
import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.excepcion.RecursoNoEncontradoException;

/**
 * Manejo centralizado de errores de los controladores web: muestra la plantilla {@code error}
 * con el código HTTP adecuado en lugar de una traza técnica.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@ControllerAdvice(basePackageClasses = ManejadorErroresWeb.class)
public class ManejadorErroresWeb {

    /**
     * @param e      excepción lanzada
     * @param modelo modelo para la vista
     * @return vista de error con código 404
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(RecursoNoEncontradoException e, Model modelo) {
        return vistaError(modelo, HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * @param e      excepción lanzada
     * @param modelo modelo para la vista
     * @return vista de error con código 403
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado(AccesoDenegadoException e, Model modelo) {
        return vistaError(modelo, HttpStatus.FORBIDDEN, e.getMessage());
    }

    /**
     * @param e      excepción lanzada
     * @param modelo modelo para la vista
     * @return vista de error con código 400
     */
    @ExceptionHandler(NegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String reglaDeNegocio(NegocioException e, Model modelo) {
        return vistaError(modelo, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private String vistaError(Model modelo, HttpStatus estado, String mensaje) {
        modelo.addAttribute("status", estado.value());
        modelo.addAttribute("error", estado.getReasonPhrase());
        modelo.addAttribute("message", mensaje);
        return "error";
    }
}
