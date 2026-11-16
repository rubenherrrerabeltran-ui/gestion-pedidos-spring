package co.sena.gestionpedidos.movil.ui.comun;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import co.sena.gestionpedidos.movil.Dependencias;
import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.datos.remoto.ApiException;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.login.LoginActivity;

/**
 * Pantalla base reutilizable (patrón Template Method): barra superior, márgenes de borde a borde,
 * indicador de carga, manejo uniforme de errores y control de sesión.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ProgressBar progreso;

    /**
     * Carga el diseño y configura la barra superior.
     *
     * @param layout      diseño XML de la pantalla (su raíz debe tener id {@code main})
     * @param titulo      título de la barra
     * @param mostrarAtras {@code true} para mostrar la flecha de regreso
     */
    protected void configurarPantalla(@LayoutRes int layout, CharSequence titulo, boolean mostrarAtras) {
        EdgeToEdge.enable(this);
        setContentView(layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        MaterialToolbar barra = findViewById(R.id.barra);
        if (barra != null) {
            setSupportActionBar(barra);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(titulo);
                getSupportActionBar().setDisplayHomeAsUpEnabled(mostrarAtras);
            }
        }
        progreso = findViewById(R.id.progreso);
    }

    /** @return el usuario en sesión, o {@code null} si no hay (y abre el inicio de sesión) */
    protected Usuario requerirSesion() {
        Usuario usuario = dependencias().getUsuario();
        if (usuario == null) {
            irAlLogin();
        }
        return usuario;
    }

    protected Dependencias dependencias() {
        return Dependencias.get();
    }

    protected void mostrarCargando(boolean cargando) {
        if (progreso != null) {
            progreso.setVisibility(cargando ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Muestra el error al usuario; si la sesión expiró, vuelve al inicio de sesión.
     *
     * @param error excepción recibida del {@link Ejecutor}
     */
    protected void mostrarError(Exception error) {
        mostrarCargando(false);
        if (error instanceof ApiException && ((ApiException) error).esSesionExpirada()) {
            dependencias().cerrarSesionLocal();
            irAlLogin();
            return;
        }
        String mensaje = error instanceof ApiException ? error.getMessage()
                : getString(R.string.error_inesperado, error.getMessage());
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_error)
                .setMessage(mensaje)
                .setPositiveButton(R.string.aceptar, null)
                .show();
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
