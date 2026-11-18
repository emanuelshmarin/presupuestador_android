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
import android.widget.EditText;

public class ClientesFragment extends Fragment {

    private RecyclerView recycler;
    private ClienteAdapter adapter;

    public ClientesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);

        recycler = view.findViewById(R.id.recyclerClientes);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        pedirClientes();

        Button btnNuevo = view.findViewById(R.id.btnNuevoCliente);
        btnNuevo.setOnClickListener(v -> mostrarDialogoAgregarCliente());

        return view;
    }

    private void pedirClientes() {
        String url = ApiClient.BASE_URL + "clientes.php";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("CLIENTES", "Respuesta: " + response.toString());
                    adapter = new ClienteAdapter(response);
                    recycler.setAdapter(adapter);
                },
                error -> {
                    Log.e("CLIENTES", "Error: " + error.toString());
                }
        );

        ApiClient.getInstance(requireContext()).getCola().add(request);
    }

    private void mostrarDialogoAgregarCliente() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_agregar_cliente, null);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // 1. Leer los datos del formulario
                    EditText etNombre = dialogView.findViewById(R.id.etNombreCliente);
                    EditText etApellido = dialogView.findViewById(R.id.etApellidoCliente);
                    EditText etTelefono = dialogView.findViewById(R.id.etTelefonoCliente);
                    EditText etEmail = dialogView.findViewById(R.id.etEmailCliente);
                    EditText etDireccion = dialogView.findViewById(R.id.etDireccionCliente);

                    String nombre = etNombre.getText().toString().trim();
                    String apellido = etApellido.getText().toString().trim();
                    String telefono = etTelefono.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String direccion = etDireccion.getText().toString().trim();

                    // 2. Validar
                    if (nombre.isEmpty()) {
                        android.widget.Toast.makeText(requireContext(), "El nombre es obligatorio", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. Armar el JSON
                    JSONObject body = new JSONObject();
                    try {
                        body.put("nombre", nombre);
                        body.put("apellido", apellido);
                        body.put("telefono", telefono);
                        body.put("email", email);
                        body.put("direccion", direccion);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(requireContext(), "Error armando los datos", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 4. Enviar el POST
                    String url = ApiClient.BASE_URL + "clientes.php";
                    com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            body,
                            response -> {
                                android.widget.Toast.makeText(requireContext(), "Cliente guardado", android.widget.Toast.LENGTH_SHORT).show();
                                pedirClientes(); // Recargar la lista
                            },
                            error -> {
                                Log.e("CLIENTES", "Error al guardar: " + error.toString());
                                android.widget.Toast.makeText(requireContext(), "Error al guardar el cliente", android.widget.Toast.LENGTH_SHORT).show();
                            }
                    );
                    ApiClient.getInstance(requireContext()).getCola().add(request);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- Adaptador interno ---
    public static class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ViewHolder> {

        private final JSONArray datos;

        public ClienteAdapter(JSONArray datos) {
            this.datos = datos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cliente, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                JSONObject cliente = datos.getJSONObject(position);
                String nombre = cliente.optString("nombre", "") + " " + cliente.optString("apellido", "");
                holder.txtNombreCliente.setText(nombre.trim());
                holder.txtTelefonoCliente.setText(cliente.optString("telefono", "-"));
                holder.txtLocalidadCliente.setText(cliente.optString("direccion", "-"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return datos.length();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombreCliente, txtTelefonoCliente, txtLocalidadCliente;

            public ViewHolder(View itemView) {
                super(itemView);
                txtNombreCliente = itemView.findViewById(R.id.txtNombreCliente);
                txtTelefonoCliente = itemView.findViewById(R.id.txtTelefonoCliente);
                txtLocalidadCliente = itemView.findViewById(R.id.txtLocalidadCliente);
            }
        }
    }
}