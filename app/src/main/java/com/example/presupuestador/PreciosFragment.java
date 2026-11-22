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
    private JSONObject precioSeleccionado = null;

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
        ApiHttp.pedir("GET", "precios.php", null, null, (codigo, cuerpo) -> {
            if (!isAdded()) return;
            Log.d("PRECIOS", "GET codigo: " + codigo);

            if (codigo == 200) {
                try {
                    adapter = new PrecioAdapter(new JSONArray(cuerpo), this);
                    recycler.setAdapter(adapter);
                } catch (Exception e) {
                    android.widget.Toast.makeText(requireContext(), "Respuesta inválida del servidor", android.widget.Toast.LENGTH_LONG).show();
                }
            } else if (codigo == -1) {
                android.widget.Toast.makeText(requireContext(), "Sin conexión con el servidor", android.widget.Toast.LENGTH_LONG).show();
            } else {
                android.widget.Toast.makeText(requireContext(), "Error al cargar precios (código " + codigo + ")", android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_agregar_precio, null);

        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoria);
        Spinner spinnerUnidad = dialogView.findViewById(R.id.spinnerUnidad);

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
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    Opcion categoriaSeleccionada = (Opcion) spinnerCategoria.getSelectedItem();
                    Opcion unidadSeleccionada = (Opcion) spinnerUnidad.getSelectedItem();
                    EditText etTrabajo = dialogView.findViewById(R.id.etTrabajo);
                    EditText etPrecio = dialogView.findViewById(R.id.etPrecio);
                    String trabajo = etTrabajo.getText().toString().trim();
                    String precioStr = etPrecio.getText().toString().trim();

                    if (categoriaSeleccionada == null || unidadSeleccionada == null) {
                        android.widget.Toast.makeText(requireContext(), "Elegí categoría y unidad", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (trabajo.isEmpty() || precioStr.isEmpty()) {
                        android.widget.Toast.makeText(requireContext(), "Completá trabajo y precio", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

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

                    ApiHttp.pedir("POST", "precios.php", body.toString(),
                            "application/json; charset=UTF-8",
                            (codigo, cuerpo) -> {
                                if (!isAdded()) return;
                                Log.d("PRECIOS", "POST codigo: " + codigo + " cuerpo: " + cuerpo);

                                if (codigo == 200 || codigo == 201) {
                                    android.widget.Toast.makeText(requireContext(), "Precio guardado", android.widget.Toast.LENGTH_SHORT).show();
                                    pedirPrecios();
                                } else if (codigo == 400) {
                                    String mensaje = "Datos inválidos";
                                    try {
                                        mensaje = new JSONObject(cuerpo).optString("error", mensaje);
                                    } catch (Exception ignorada) { }
                                    android.widget.Toast.makeText(requireContext(), mensaje, android.widget.Toast.LENGTH_LONG).show();
                                } else if (codigo == -1) {
                                    android.widget.Toast.makeText(requireContext(), "Sin conexión con el servidor", android.widget.Toast.LENGTH_LONG).show();
                                } else {
                                    android.widget.Toast.makeText(requireContext(), "Error (código " + codigo + ")", android.widget.Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoOpciones(JSONObject precio) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("¿Qué querés hacer?")
                .setItems(new String[]{"Modificar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        android.widget.Toast.makeText(requireContext(), "Modificar (próximo paso)", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        confirmarEliminar(precio);   // <-- CAMBIO: antes era un Toast
                    }
                })
                .show();
    }

    private void confirmarEliminar(JSONObject precio) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar precio")
                .setMessage("¿Seguro que querés eliminar \"" + precio.optString("trabajo") + "\"?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarPrecio(precio.optInt("id_trabajo")))  // <-- id_trabajo
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPrecio(int idTrabajo) {
        ApiHttp.pedir("DELETE", "precios.php",
                "id_trabajo=" + idTrabajo,
                "application/x-www-form-urlencoded; charset=UTF-8",
                (codigo, cuerpo) -> {
                    if (!isAdded()) return;   // por si el fragment ya no está en pantalla
                    Log.d("PRECIOS", "DELETE codigo: " + codigo + " cuerpo: " + cuerpo);

                    if (codigo == 200) {
                        android.widget.Toast.makeText(requireContext(), "Precio eliminado", android.widget.Toast.LENGTH_SHORT).show();
                        pedirPrecios();
                    } else if (codigo == 409) {
                        android.widget.Toast.makeText(requireContext(), "No se puede eliminar: ya está en un presupuesto", android.widget.Toast.LENGTH_LONG).show();
                    } else if (codigo == -1) {
                        android.widget.Toast.makeText(requireContext(), "Sin conexión con el servidor", android.widget.Toast.LENGTH_LONG).show();
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Error (código " + codigo + ")", android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- Adaptador interno ---
    public static class PrecioAdapter extends RecyclerView.Adapter<PrecioAdapter.ViewHolder> {

        private final JSONArray datos;
        private final PreciosFragment fragment;

        public PrecioAdapter(JSONArray datos, PreciosFragment fragment) {
            this.datos = datos;
            this.fragment = fragment;
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
                holder.itemView.setOnClickListener(v -> {
                    fragment.precioSeleccionado = precio;
                    fragment.mostrarDialogoOpciones(precio);
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