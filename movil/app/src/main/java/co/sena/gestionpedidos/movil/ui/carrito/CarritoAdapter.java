package co.sena.gestionpedidos.movil.ui.carrito;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.carrito.ItemCarrito;
import co.sena.gestionpedidos.movil.util.Formatos;

/**
 * Adaptador de los productos del carrito con botones para sumar y restar unidades.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.Vista> {

    /** Acción al cambiar la cantidad de un producto. */
    public interface AlCambiar {
        void cambiar(ItemCarrito item, int cambio);
    }

    private final List<ItemCarrito> items = new ArrayList<>();
    private final AlCambiar alCambiar;

    public CarritoAdapter(AlCambiar alCambiar) {
        this.alCambiar = alCambiar;
    }

    /** Reemplaza los elementos mostrados en la lista. */
    public void mostrar(List<ItemCarrito> nuevos) {
        items.clear();
        items.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Vista onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Vista(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_carrito, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vista vista, int posicion) {
        ItemCarrito item = items.get(posicion);
        vista.nombre.setText(item.getProducto().getNombre());
        vista.precio.setText(vista.itemView.getContext().getString(R.string.precio_unitario,
                Formatos.moneda(item.getProducto().getPrecio())));
        vista.cantidad.setText(String.valueOf(item.getCantidad()));
        vista.subtotal.setText(Formatos.moneda(item.getSubtotal()));
        vista.menos.setOnClickListener(v -> alCambiar.cambiar(item, -1));
        vista.mas.setOnClickListener(v -> alCambiar.cambiar(item, 1));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Vista extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView precio;
        final TextView cantidad;
        final TextView subtotal;
        final View menos;
        final View mas;

        Vista(View v) {
            super(v);
            nombre = v.findViewById(R.id.textoNombre);
            precio = v.findViewById(R.id.textoPrecio);
            cantidad = v.findViewById(R.id.textoCantidad);
            subtotal = v.findViewById(R.id.textoSubtotal);
            menos = v.findViewById(R.id.botonMenos);
            mas = v.findViewById(R.id.botonMas);
        }
    }
}
