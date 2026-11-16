package co.sena.gestionpedidos.movil.ui.productos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.Producto;
import co.sena.gestionpedidos.movil.util.Formatos;

/**
 * Adaptador (patrón Adapter) que convierte la lista de productos en tarjetas del RecyclerView.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.Vista> {

    /** Acción al pulsar "Agregar" en un producto. */
    public interface AlAgregar {
        void agregar(Producto producto);
    }

    private final List<Producto> productos = new ArrayList<>();
    private final boolean permiteAgregar;
    private final AlAgregar alAgregar;

    public ProductosAdapter(boolean permiteAgregar, AlAgregar alAgregar) {
        this.permiteAgregar = permiteAgregar;
        this.alAgregar = alAgregar;
    }

    /** @param nuevos productos que se muestran */
    public void mostrar(List<Producto> nuevos) {
        productos.clear();
        productos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Vista onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Vista(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_producto, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vista vista, int posicion) {
        Producto p = productos.get(posicion);
        vista.nombre.setText(p.getNombre());
        vista.precio.setText(Formatos.moneda(p.getPrecio()));
        if (!p.isActivo()) {
            vista.stock.setText(R.string.producto_inactivo);
        } else {
            vista.stock.setText(vista.itemView.getContext().getString(
                    p.tieneStockBajo() ? R.string.stock_bajo : R.string.stock, p.getStock()));
        }
        vista.stock.setTextColor(ContextCompat.getColor(vista.itemView.getContext(),
                p.tieneStockBajo() || !p.isActivo() ? R.color.rojo : R.color.gris));
        vista.agregar.setVisibility(permiteAgregar ? View.VISIBLE : View.GONE);
        vista.agregar.setEnabled(p.estaDisponible());
        vista.agregar.setOnClickListener(v -> alAgregar.agregar(p));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static final class Vista extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView precio;
        final TextView stock;
        final Button agregar;

        Vista(View v) {
            super(v);
            nombre = v.findViewById(R.id.textoNombre);
            precio = v.findViewById(R.id.textoPrecio);
            stock = v.findViewById(R.id.textoStock);
            agregar = v.findViewById(R.id.botonAgregar);
        }
    }
}
