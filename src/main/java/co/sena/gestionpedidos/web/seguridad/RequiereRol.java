package co.sena.gestionpedidos.web.seguridad;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.sena.gestionpedidos.modelo.Rol;

/**
 * Restringe un controlador o un método a los roles indicados.
 * {@link SesionInterceptor} la lee antes de ejecutar el método; si el usuario no tiene el rol
 * se responde 403 (acceso denegado). Si se pone en la clase y en el método, prevalece la del método.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiereRol {

    /**
     * @return roles que pueden ejecutar la operación
     */
    Rol[] value();
}
