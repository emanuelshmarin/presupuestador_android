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
import org.json.JSONArray;
import org.json.JSONObject;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Toast;

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

    // Arma el texto del Toast según el código y el cuerpo de la respuesta
    private String mensajeError(int codigo, String cuerpo) {
        if (codigo == -1) {
            return "Sin conexión con el servidor";
        }
        try {
            String mensajeApi = new JSONObject(cuerpo).optString("error", "");
            if (!mensajeApi.isEmpty()) return mensajeApi;
        } catch (Exception ignorada) { }
        return "Error (código " + codigo + ")";
    }

    private void pedirPresupuestos() {
        ApiHttp.pedir("GET", "presupuestos.php", null, null, (codigo, cuerpo) -> {
            if (!isAdded()) return;
            Log.d("PRESUPUESTOS", "GET codigo: " + codigo);

            if (codigo == 200) {
                try {
                    listaPresupuestosCompleta = new JSONArray(cuerpo);
                    adapter = new PresupuestoAdapter(listaPresupuestosCompleta);
                    recycler.setAdapter(adapter);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Respuesta inválida del servidor", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), mensajeError(codigo, cuerpo), Toast.LENGTH_LONG).show();
            }
        });
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

    private void cargarClientes(Spinner spinnerCliente) {
        ApiHttp.pedir("GET", "clientes.php", null, null, (codigo, cuerpo) -> {
            if (!isAdded()) return;

            if (codigo == 200) {
                try {
                    JSONArray lista = new JSONArray(cuerpo);
                    listaClientes.clear();
                    for (int i = 0; i < lista.length(); i++) {
                        JSONObject obj = lista.getJSONObject(i);
                        String nombreCompleto = obj.optString("nombre", "") + " " + obj.optString("apellido", "");
                        listaClientes.add(new Opcion(obj.getInt("id"), nombreCompleto.trim()));
                    }
                    ArrayAdapter<Opcion> adapterCliente = new ArrayAdapter<>(
                            requireContext(), android.R.layout.simple_spinner_item);
                    adapterCliente.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    adapterCliente.addAll(listaClientes);
                    spinnerCliente.setAdapter(adapterCliente);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Respuesta inválida del servidor", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), mensajeError(codigo, cuerpo), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarTrabajos(LinearLayout contenedorTrabajos) {
        ApiHttp.pedir("GET", "precios.php", null, null, (codigo, cuerpo) -> {
            if (!isAdded()) return;

            if (codigo == 200) {
                try {
                    JSONArray lista = new JSONArray(cuerpo);
                    listaTrabajos.clear();
                    for (int i = 0; i < lista.length(); i++) {
                        JSONObject obj = lista.getJSONObject(i);
                        String descripcion = obj.optString("trabajo", "-") + " ($" + obj.optString("precio", "0") + ")";
                        listaTrabajos.add(new Opcion(obj.getInt("id_trabajo"), descripcion));
                    }
                    // Agregar una fila de trabajo por defecto
                    agregarFilaTrabajo(contenedorTrabajos);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Respuesta inválida del servidor", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), mensajeError(codigo, cuerpo), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoAgregarPresupuesto() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_agregar_presupuesto, null);

        Spinner spinnerCliente = dialogView.findViewById(R.id.spinnerCliente);
        LinearLayout contenedorTrabajos = dialogView.findViewById(R.id.contenedorTrabajos);
        Button btnAgregarTrabajo = dialogView.findViewById(R.id.btnAgregarTrabajo);

        cargarClientes(spinnerCliente);
        cargarTrabajos(contenedorTrabajos);

        // Botón para agregar otra fila de trabajo
        btnAgregarTrabajo.setOnClickListener(v -> agregarFilaTrabajo(contenedorTrabajos));

        // Mostrar el diálogo
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // 1. Leer el cliente seleccionado
                    Opcion clienteSeleccionado = (Opcion) spinnerCliente.getSelectedItem();
                    if (clienteSeleccionado == null) {
                        Toast.makeText(requireContext(), "Elegí un cliente", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(requireContext(), "Completá todos los trabajos y cantidades", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONObject item = new JSONObject();
                            item.put("id_trabajo", trabajoSeleccionado.id);
                            item.put("cantidad", Integer.parseInt(cantidadStr));
                            items.put(item);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Error armando los items", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (items.length() == 0) {
                        Toast.makeText(requireContext(), "Agregá al menos un trabajo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Armar el JSON completo
                    JSONObject body = new JSONObject();
                    try {
                        body.put("id_cliente", clienteSeleccionado.id);
                        body.put("items", items);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error armando los datos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 4. Enviar el POST
                    ApiHttp.pedir("POST", "presupuestos.php", body.toString(),
                            "application/json; charset=UTF-8",
                            (codigo, cuerpo) -> {
                                if (!isAdded()) return;
                                Log.d("PRESUPUESTOS", "POST codigo: " + codigo + " cuerpo: " + cuerpo);

                                if (codigo == 200 || codigo == 201) {
                                    Toast.makeText(requireContext(), "Presupuesto guardado", Toast.LENGTH_SHORT).show();
                                    pedirPresupuestos(); // Recargar la lista
                                } else {
                                    Toast.makeText(requireContext(), mensajeError(codigo, cuerpo), Toast.LENGTH_LONG).show();
                                }
                            });
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
                holder.itemView.setOnClickListener(v -> {
                    android.util.Log.d("PRESUPUESTOS", "JSON tocado: " + presupuesto.toString());
                    android.content.Intent intent = new android.content.Intent(
                            v.getContext(), DetallePresupuestoActivity.class);
                    intent.putExtra(DetallePresupuestoActivity.EXTRA_ID, presupuesto.optInt("id"));
                    intent.putExtra(DetallePresupuestoActivity.EXTRA_NRO, presupuesto.optString("nro", "-"));
                    v.getContext().startActivity(intent);
                });
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