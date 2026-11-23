package com.example.presupuestador;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

public class ConfiguracionFragment extends Fragment {

    public ConfiguracionFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // Botón Cerrar Sesión
        Button btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cerrar sesión")
                    .setMessage("¿Estás seguro que querés cerrar sesión?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Botón Eliminar Cuenta
        Button btnEliminarCuenta = view.findViewById(R.id.btnEliminarCuenta);
        btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminarCuenta());

        return view;
    }

    private void mostrarDialogoEliminarCuenta() {
        EditText etPassword = new EditText(requireContext());
        etPassword.setHint("Contraseña");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cuenta")
                .setMessage("Para eliminar tu cuenta, escribí tu contraseña. Se borrarán también todos tus datos.")
                .setView(etPassword)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String password = etPassword.getText().toString().trim();
                    if (password.isEmpty()) {
                        Toast.makeText(requireContext(), "Tenés que escribir tu contraseña", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new AlertDialog.Builder(requireContext())
                            .setTitle("¿Seguro?")
                            .setMessage("¿Seguro que querés eliminar tu cuenta? Esta acción no se puede deshacer.")
                            .setPositiveButton("Sí, eliminar", (d, w) -> eliminarCuenta(password))
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuenta(String password) {
        String url = ApiClient.BASE_URL + "usuarios.php";

        JSONObject body = new JSONObject();
        try {
            body.put("accion", "eliminar_cuenta");
            body.put("password", password);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error armando los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                },
                error -> {
                    Log.e("CONFIG", "Error al eliminar cuenta: " + error.toString());
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String respuesta = new String(error.networkResponse.data);
                        Log.e("CONFIG", "Respuesta del servidor: " + respuesta);
                        try {
                            JSONObject jsonError = new JSONObject(respuesta);
                            String mensaje = jsonError.optString("error", "Error al eliminar la cuenta");
                            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        ApiClient.getInstance(requireContext()).getCola().add(request);
    }
}