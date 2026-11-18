package com.example.presupuestador;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONObject;

public class PresupuestoFragment extends Fragment {

    private RecyclerView recycler;
    private PresupuestoAdapter adapter;

    public PresupuestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_presupuesto, container, false);

        recycler = view.findViewById(R.id.recyclerPresupuestos);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        pedirPresupuestos();

        Button btnNuevo = view.findViewById(R.id.btnNuevoPresupuesto);
        btnNuevo.setOnClickListener(v -> mostrarDialogoAgregarPresupuesto());

        return view;
    }

    private void pedirPresupuestos() {
        String url = ApiClient.BASE_URL + "presupuestos.php";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PRESUPUESTOS", "Respuesta: " + response.toString());
                    adapter = new PresupuestoAdapter(response);
                    recycler.setAdapter(adapter);
                },
                error -> {
                    Log.e("PRESUPUESTOS", "Error: " + error.toString());
                }
        );

        ApiClient.getInstance(requireContext()).getCola().add(request);
    }

    private void mostrarDialogoAgregarPresupuesto() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_agregar_presupuesto, null);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    android.widget.Toast.makeText(requireContext(), "Guardar presupuesto tocado", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- Adaptador interno ---
    public static class PresupuestoAdapter extends RecyclerView.Adapter<PresupuestoAdapter.ViewHolder> {

        private final JSONArray datos;

        public PresupuestoAdapter(JSONArray datos) {
            this.datos = datos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_presupuesto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                JSONObject presupuesto = datos.getJSONObject(position);
                holder.txtNro.setText(presupuesto.optString("nro", "-"));
                holder.txtCliente.setText(presupuesto.optString("cliente", "-"));
                holder.txtTotal.setText("$" + presupuesto.optString("total", "0"));
                holder.txtEstado.setText(presupuesto.optString("estado", "-"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return datos.length();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNro, txtCliente, txtTotal, txtEstado;

            public ViewHolder(View itemView) {
                super(itemView);
                txtNro = itemView.findViewById(R.id.txtNro);
                txtCliente = itemView.findViewById(R.id.txtCliente);
                txtTotal = itemView.findViewById(R.id.txtTotal);
                txtEstado = itemView.findViewById(R.id.txtEstado);
            }
        }
    }
}