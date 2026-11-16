package co.sena.gestionpedidos.movil.ui.pedidos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.sena.gestionpedidos.movil.R;
import co.sena.gestionpedidos.movil.dominio.modelo.Pedido;
import co.sena.gestionpedidos.movil.ui.comun.Estilos;
import co.sena.gestionpedidos.movil.util.Formatos;

/**
 * Adaptador de la lista de pedidos.
 *
 * @author Rubén Darío Herrera Beltrán
 */
public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.Vista> {

    /** Acción al tocar un pedido. */
    public interface AlSeleccionar {
        void seleccionar(Pedido pedido);
    }

    private final List<Pedido> pedidos = new ArrayList<>();
    private final AlSeleccionar alSeleccionar;

    public PedidosAdapter(AlSeleccionar alSeleccionar) {
        this.alSeleccionar = alSeleccionar;
    }

    /** Reemplaza los elementos mostrados en la lista. */
    public void mostrar(List<Pedido> nuevos) {
        pedidos.clear();
        pedidos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Vista onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Vista(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_pedido, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Vista vista, int posicion) {
        Pedido p = pedidos.get(posicion);
        vista.numero.setText(vista.itemView.getContext().getString(R.string.numero_pedido, p.getIdPedido()));
        vista.cliente.setText(p.getNombreCliente());
        vista.fecha.setText(vista.itemView.getContext().getString(R.string.fecha_unidades,
                Formatos.fechaHora(p.getFechaPedido()), Formatos.unidades(p.getUnidades())));
        vista.total.setText(Formatos.moneda(p.getTotal()));
        Estilos.pintarEstado(vista.estado, p.getEstado());
        vista.itemView.setOnClickListener(v -> alSeleccionar.seleccionar(p));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static final class Vista extends RecyclerView.ViewHolder {
        final TextView numero;
        final TextView cliente;
        final TextView fecha;
        final TextView total;
        final TextView estado;

        Vista(View v) {
            super(v);
            numero = v.findViewById(R.id.textoNumero);
            cliente = v.findViewById(R.id.textoCliente);
            fecha = v.findViewById(R.id.textoFecha);
            total = v.findViewById(R.id.textoTotal);
            estado = v.findViewById(R.id.textoEstado);
        }
    }
}
