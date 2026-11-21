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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.EditText;

public class PresupuestoFragment extends Fragment {

    private RecyclerView recycler;
    private PresupuestoAdapter adapter;
    private JSONArray listaPresupuestosCompleta = new JSONArray();
    private java.util.List<Opcion> listaClientes = new java.util.ArrayList<>();
    private java.util.List<Opcion> listaTrabajos = new java.util.ArrayList<>();

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

        EditText etBuscar = view.findViewById(R.id.etBuscar);
        etBuscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPresupuestos(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

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
                    listaPresupuestosCompleta = response;
                    adapter = new PresupuestoAdapter(response);
                    recycler.setAdapter(adapter);
                },
                error -> {
                    Log.e("PRESUPUESTOS", "Error: " + error.toString());
                }
        );

        ApiClient.getInstance(requireContext()).getCola().add(request);
    }

    private void filtrarPresupuestos(String texto) {
        if (listaPresupuestosCompleta == null) return;

        JSONArray filtrados = new JSONArray();
        for (int i = 0; i < listaPresupuestosCompleta.length(); i++) {
            try {
                JSONObject presupuesto = listaPresupuestosCompleta.getJSONObject(i);
                String nro = presupuesto.optString("nro", "");
                String cliente = presupuesto.optString("cliente", "");
                if (nro.toLowerCase().contains(texto.toLowerCase()) ||
                        cliente.toLowerCase().contains(texto.toLowerCase())) {
                    filtrados.put(presupuesto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        adapter = new PresupuestoAdapter(filtrados);
        recycler.setAdapter(adapter);
    }

    private void mostrarDialogoAgregarPresupuesto() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_agregar_presupuesto, null);

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerCliente);
        LinearLayout contenedorTrabajos = dialogView.findViewById(R.id.contenedorTrabajos);
        Button btnAgregarTrabajo = dialogView.findViewById(R.id.btnAgregarTrabajo);

        // Pedir clientes
        String urlClientes = ApiClient.BASE_URL + "clientes.php";
        JsonArrayRequest requestClientes = new JsonArrayRequest(
                Request.Method.GET,
                urlClientes,
                null,
                response -> {
                    listaClientes.clear();
                    ArrayAdapter<Opcion> adapterCliente = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item
                    );
                    adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String nombreCompleto = obj.optString("nombre", "") + " " + obj.optString("apellido", "");
                            listaClientes.add(new Opcion(obj.getInt("id"), nombreCompleto.trim()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapterCliente.addAll(listaClientes);
                    spinnerCliente.setAdapter(adapterCliente);
                },
                error -> Log.e("PRESUPUESTOS", "Error clientes: " + error.toString())
        );
        ApiClient.getInstance(requireContext()).getCola().add(requestClientes);

        // Pedir trabajos (precios)
        String urlTrabajos = ApiClient.BASE_URL + "precios.php";
        JsonArrayRequest requestTrabajos = new JsonArrayRequest(
                Request.Method.GET,
                urlTrabajos,
                null,
                response -> {
                    listaTrabajos.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String descripcion = obj.optString("trabajo", "-") + " ($" + obj.optString("precio", "0") + ")";
                            listaTrabajos.add(new Opcion(obj.getInt("id_trabajo"), descripcion));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // Agregar una fila de trabajo por defecto
                    agregarFilaTrabajo(contenedorTrabajos);
                },
                error -> Log.e("PRESUPUESTOS", "Error trabajos: " + error.toString())
        );
        ApiClient.getInstance(requireContext()).getCola().add(requestTrabajos);

        // Botón para agregar otra fila de trabajo
        btnAgregarTrabajo.setOnClickListener(v -> agregarFilaTrabajo(contenedorTrabajos));

        // Mostrar el diálogo
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // 1. Leer el cliente seleccionado
                    Opcion clienteSeleccionado = (Opcion) spinnerCliente.getSelectedItem();
                    if (clienteSeleccionado == null) {
                        android.widget.Toast.makeText(requireContext(), "Elegí un cliente", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2. Armar el array de items recorriendo las filas
                    JSONArray items = new JSONArray();
                    for (int i = 0; i < contenedorTrabajos.getChildCount(); i++) {
                        View fila = contenedorTrabajos.getChildAt(i);
                        Spinner spinnerTrabajo = fila.findViewById(R.id.spinnerTrabajo);
                        EditText etCantidad = fila.findViewById(R.id.etCantidad);

                        Opcion trabajoSeleccionado = (Opcion) spinnerTrabajo.getSelectedItem();
                        String cantidadStr = etCantidad.getText().toString().trim();

                        if (trabajoSeleccionado == null || cantidadStr.isEmpty()) {
                            android.widget.Toast.makeText(requireContext(), "Completá todos los trabajos y cantidades", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONObject item = new JSONObject();
                            item.put("id_trabajo", trabajoSeleccionado.id);
                            item.put("cantidad", Integer.parseInt(cantidadStr));
                            items.put(item);
                        } catch (Exception e) {
                            android.widget.Toast.makeText(requireContext(), "Error armando los items", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (items.length() == 0) {
                        android.widget.Toast.makeText(requireContext(), "Agregá al menos un trabajo", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Armar el JSON completo
                    JSONObject body = new JSONObject();
                    try {
                        body.put("id_cliente", clienteSeleccionado.id);
                        body.put("items", items);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(requireContext(), "Error armando los datos", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 4. Enviar el POST
                    String url = ApiClient.BASE_URL + "presupuestos.php";
                    com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            body,
                            response -> {
                                android.widget.Toast.makeText(requireContext(), "Presupuesto guardado", android.widget.Toast.LENGTH_SHORT).show();
                                pedirPresupuestos(); // Recargar la lista
                            },
                            error -> {
                                Log.e("PRESUPUESTOS", "Error al guardar: " + error.toString());
                                if (error.networkResponse != null && error.networkResponse.data != null) {
                                    Log.e("PRESUPUESTOS", "Respuesta del servidor: " + new String(error.networkResponse.data));
                                }
                                android.widget.Toast.makeText(requireContext(), "Error al guardar el presupuesto", android.widget.Toast.LENGTH_SHORT).show();
                            }
                    );
                    ApiClient.getInstance(requireContext()).getCola().add(request);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- Método que agrega una fila de trabajo ---
    private void agregarFilaTrabajo(LinearLayout contenedor) {
        View fila = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_trabajo_presupuesto, contenedor, false);

        Spinner spinnerTrabajo = fila.findViewById(R.id.spinnerTrabajo);
        ArrayAdapter<Opcion> adapterTrabajo = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item
        );
        adapterTrabajo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterTrabajo.addAll(listaTrabajos);
        spinnerTrabajo.setAdapter(adapterTrabajo);

        contenedor.addView(fila);
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