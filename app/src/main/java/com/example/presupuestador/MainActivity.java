package com.example.presupuestador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnIngresar = findViewById(R.id.btnIngresar);
        TextView txtIrRegistro = findViewById(R.id.txtIrRegistro);

        // Acción para ir a Registro
        txtIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        // Acción para el botón Ingresar
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerLogin(); // Llamamos a nuestro método
            }
        });
    }

    // --- ACÁ EMPIEZA LA LÓGICA DEL LOGIN ---
    private void hacerLogin() {
        // 1. Leer los campos
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 2. Validar en el cliente
        if (email.isEmpty()) {
            etEmail.setError("Ingresá tu email");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingresá tu contraseña");
            etPassword.requestFocus();
            return;
        }

        // 3. Armar el JSON
        JSONObject body = new JSONObject();
        try {
            body.put("email_usuario", email);
            body.put("password_usuario", password);
        } catch (Exception e) {
            Toast.makeText(this, "Error armando la petición", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Armar la URL
        String url = ApiClient.BASE_URL + "login.php";

        // 5. Crear la petición POST con Volley
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> manejarRespuestaExitosa(response),
                error -> manejarError(error)
        );

        // 6. Encolar la petición
        ApiClient.getInstance(this).getCola().add(request);
    }

    // --- MANEJO DE RESPUESTAS ---
    private void manejarRespuestaExitosa(JSONObject response) {
        String nombre = response.optString("nombre", "usuario");
        Toast.makeText(this, "¡Bienvenido, " + nombre + "!", Toast.LENGTH_LONG).show();
    }

    private void manejarError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String jsonStr = new String(error.networkResponse.data, "UTF-8");
                JSONObject jsonError = new JSONObject(jsonStr);
                String mensaje = jsonError.optString("error", "Credenciales inválidas");
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error del servidor", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No se pudo conectar al servidor", Toast.LENGTH_LONG).show();
        }
    }
}