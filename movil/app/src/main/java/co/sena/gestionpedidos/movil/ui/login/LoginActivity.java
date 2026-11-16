package co.sena.gestionpedidos.movil.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.Usuario;
import co.sena.gestionpedidos.movil.ui.comun.BaseActivity;
import co.sena.gestionpedidos.movil.ui.comun.Ejecutor;
import co.sena.gestionpedidos.movil.ui.inicio.InicioActivity;
import co.sena.gestionpedidos.movil.util.Validador;

/**
 * Módulo de autenticación: pide la dirección del servidor, el correo y la contraseña,
 * los valida y llama a POST /api/sesion.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class LoginActivity extends BaseActivity {

    private TextInputLayout capaServidor;
    private TextInputLayout capaCorreo;
    private TextInputLayout capaClave;
    private Button botonIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependencias().getUsuario() != null) {
            abrirInicio();
            return;
        }
        configurarPantalla(R.layout.activity_login, getString(R.string.titulo_login), false);
        capaServidor = findViewById(R.id.capaServidor);
        capaCorreo = findViewById(R.id.capaCorreo);
        capaClave = findViewById(R.id.capaClave);
        botonIngresar = findViewById(R.id.botonIngresar);

        texto(capaServidor, dependencias().getPreferencias().getUrlServidor());
        texto(capaCorreo, dependencias().getPreferencias().getUltimoCorreo());
        botonIngresar.setOnClickListener(v -> ingresar());
    }

    private void ingresar() {
        String servidor = leer(capaServidor);
        String correo = leer(capaCorreo);
        String clave = leer(capaClave);
        capaServidor.setError(Validador.validarUrlServidor(servidor));
        capaCorreo.setError(Validador.validarCorreo(correo));
        capaClave.setError(Validador.validarClave(clave));
        if (capaServidor.getError() != null || capaCorreo.getError() != null || capaClave.getError() != null) {
            return;
        }
        dependencias().configurarServidor(servidor);
        dependencias().getPreferencias().setUltimoCorreo(correo);
        botonIngresar.setEnabled(false);
        mostrarCargando(true);
        dependencias().getEjecutor().ejecutar(
                () -> dependencias().getApi().iniciarSesion(correo, clave),
                new Ejecutor.Resultado<Usuario>() {
                    @Override
                    public void exito(Usuario usuario) {
                        dependencias().setUsuario(usuario);
                        abrirInicio();
                    }

                    @Override
                    public void error(Exception error) {
                        botonIngresar.setEnabled(true);
                        mostrarError(error);
                    }
                });
    }

    private void abrirInicio() {
        startActivity(new Intent(this, InicioActivity.class));
        finish();
    }

    private static String leer(TextInputLayout capa) {
        return capa.getEditText() == null ? "" : capa.getEditText().getText().toString();
    }

    private static void texto(TextInputLayout capa, String valor) {
        if (capa.getEditText() != null) {
            capa.getEditText().setText(valor);
        }
    }

    @Override
    protected void mostrarCargando(boolean cargando) {
        super.mostrarCargando(cargando);
        View formulario = findViewById(R.id.formulario);
        if (formulario != null) {
            formulario.setAlpha(cargando ? 0.6f : 1f);
        }
    }
}
