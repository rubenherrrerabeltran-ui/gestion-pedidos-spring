package co.sena.gestionpedidos.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.sena.gestionpedidos.excepcion.AccesoDenegadoException;
import co.sena.gestionpedidos.excepcion.NegocioException;
import co.sena.gestionpedidos.excepcion.RecursoNoEncontradoException;

/**
 * Convierte las excepciones de la API en respuestas JSON con el formato estándar
 * <em>Problem Details</em> (RFC 9457) que ofrece Spring con {@link ProblemDetail}.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RestControllerAdvice(basePackageClasses = ManejadorErroresApi.class)
public class ManejadorErroresApi {

    /**
     * @param e excepción
     * @return 404 Not Found
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * @param e excepción
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail accesoDenegado(AccesoDenegadoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /**
     * @param e excepción
     * @return 422 Unprocessable Content: la petición es correcta pero viola una regla de negocio
     */
    @ExceptionHandler(NegocioException.class)
    public ProblemDetail reglaDeNegocio(NegocioException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    /**
     * @param e excepción
     * @return 400 Bad Request cuando el JSON está mal formado
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo JSON no es válido");
    }
}
