package co.sena.gestionpedidos.movil;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.sena.gestionpedidos.movil.ui.login.LoginActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Prueba instrumentada (Espresso) de la pantalla de inicio de sesión: se ejecuta en el emulador.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> regla = new ActivityScenarioRule<>(LoginActivity.class);

    /** Verifica que muestra errores si faltan datos. */
    @Test
    public void muestraErroresSiFaltanDatos() {
        onView(withId(R.id.campoCorreo)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.campoClave)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.botonIngresar)).perform(click());
        onView(withText("Escriba su correo")).check(matches(isDisplayed()));
        onView(withText("Escriba su contraseña")).check(matches(isDisplayed()));
    }
}
