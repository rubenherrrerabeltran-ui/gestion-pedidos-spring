package co.sena.gestionpedidos.web.seguridad;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un controlador o método que se puede usar sin iniciar sesión
 * (pantalla de ingreso y consulta pública del catálogo en la API).
 *
 * @author Rubén Darío Herrera Beltrán
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Publico {
}
