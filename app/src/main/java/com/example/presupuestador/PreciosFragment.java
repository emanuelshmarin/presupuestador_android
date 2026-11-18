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
import android.widget.Spinner;
import android.widget.EditText;

public class PreciosFragment extends Fragment {

    private RecyclerView recycler;
    private PrecioAdapter adapter;

    public PreciosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_precios, container, false);

        recycler = view.findViewById(R.id.recyclerPrecios);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        pedirPrecios();

        Button btnAgregar = view.findViewById(R.id.btnAgregarPrecio);
        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        return view;
    }

    private void pedirPrecios() {
        String url = ApiClient.BASE_URL + "precios.php";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("PRECIOS", "Respuesta: " + response.toString());
                    adapter = new PrecioAdapter(response);
                    recycler.setAdapter(adapter);
                },
                error -> {
                    Log.e("PRECIOS", "Error: " + error.toString());
                }
        );

        ApiClient.getInstance(requireContext()).getCola().add(request);
    }

    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_agregar_precio, null);

        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoria);
        Spinner spinnerUnidad = dialogView.findViewById(R.id.spinnerUnidad);

        // Pedir categorías al servidor
        String urlCategorias = ApiClient.BASE_URL + "categorias.php";
        JsonArrayRequest requestCategorias = new JsonArrayRequest(
                Request.Method.GET,
                urlCategorias,
                null,
                response -> {
                    ArrayAdapter<Opcion> adapterCategoria = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item
                    );
                    adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            adapterCategoria.add(new Opcion(obj.getInt("id"), obj.getString("descripcion")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    spinnerCategoria.setAdapter(adapterCategoria);
                },
                error -> Log.e("CATEGORIAS", "Error: " + error.toString())
        );
        ApiClient.getInstance(requireContext()).getCola().add(requestCategorias);

        // Pedir unidades al servidor
        String urlUnidades = ApiClient.BASE_URL + "unidades.php";
        JsonArrayRequest requestUnidades = new JsonArrayRequest(
                Request.Method.GET,
                urlUnidades,
                null,
                response -> {
                    ArrayAdapter<Opcion> adapterUnidad = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item
                    );
                    adapterUnidad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            adapterUnidad.add(new Opcion(obj.getInt("id"), obj.getString("descripcion")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    spinnerUnidad.setAdapter(adapterUnidad);
                },
                error -> Log.e("UNIDADES", "Error: " + error.toString())
        );
        ApiClient.getInstance(requireContext()).getCola().add(requestUnidades);

        // Mostrar el diálogo
        // Mostrar el diálogo
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // 1. Leer los datos del formulario
                    Opcion categoriaSeleccionada = (Opcion) spinnerCategoria.getSelectedItem();
                    Opcion unidadSeleccionada = (Opcion) spinnerUnidad.getSelectedItem();
                    EditText etTrabajo = dialogView.findViewById(R.id.etTrabajo);
                    EditText etPrecio = dialogView.findViewById(R.id.etPrecio);
                    String trabajo = etTrabajo.getText().toString().trim();
                    String precioStr = etPrecio.getText().toString().trim();

                    // 2. Validar
                    if (categoriaSeleccionada == null || unidadSeleccionada == null) {
                        android.widget.Toast.makeText(requireContext(), "Elegí categoría y unidad", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (trabajo.isEmpty() || precioStr.isEmpty()) {
                        android.widget.Toast.makeText(requireContext(), "Completá trabajo y precio", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Armar el JSON
                    JSONObject body = new JSONObject();
                    try {
                        body.put("id_categoria", categoriaSeleccionada.id);
                        body.put("id_unidad", unidadSeleccionada.id);
                        body.put("trabajo", trabajo);
                        body.put("precio", Double.parseDouble(precioStr));
                    } catch (Exception e) {
                        android.widget.Toast.makeText(requireContext(), "Error armando los datos", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 4. Enviar el POST
                    String url = ApiClient.BASE_URL + "precios.php";
                    com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            body,
                            response -> {
                                android.widget.Toast.makeText(requireContext(), "Precio guardado", android.widget.Toast.LENGTH_SHORT).show();
                                pedirPrecios(); // Recargar la lista
                            },
                            error -> {
                                Log.e("PRECIOS", "Error al guardar: " + error.toString());
                                android.widget.Toast.makeText(requireContext(), "Error al guardar el precio", android.widget.Toast.LENGTH_SHORT).show();
                            }
                    );
                    ApiClient.getInstance(requireContext()).getCola().add(request);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    // --- Adaptador interno ---
    public static class PrecioAdapter extends RecyclerView.Adapter<PrecioAdapter.ViewHolder> {

        private final JSONArray datos;

        public PrecioAdapter(JSONArray datos) {
            this.datos = datos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_precio, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                JSONObject precio = datos.getJSONObject(position);
                holder.txtCategoria.setText(precio.optString("categoria", "-"));
                holder.txtTrabajo.setText(precio.optString("trabajo", "-"));
                holder.txtUnidad.setText(precio.optString("unidad", "-"));
                holder.txtPrecio.setText("$" + precio.optString("precio", "0"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return datos.length();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtCategoria, txtTrabajo, txtUnidad, txtPrecio;

            public ViewHolder(View itemView) {
                super(itemView);
                txtCategoria = itemView.findViewById(R.id.txtCategoria);
                txtTrabajo = itemView.findViewById(R.id.txtTrabajo);
                txtUnidad = itemView.findViewById(R.id.txtUnidad);
                txtPrecio = itemView.findViewById(R.id.txtPrecio);
            }
        }
    }
}