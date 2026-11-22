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

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        TextView txtIrRegistro = findViewById(R.id.txtIrRegistro);

        txtIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistroActivity.class));
            }
        });

        btnIngresar.setOnClickListener(v -> hacerLogin());
    }

    private void hacerLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

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

        JSONObject body = new JSONObject();
        try {
            body.put("email_usuario", email);
            body.put("password_usuario", password);
        } catch (Exception e) {
            Toast.makeText(this, "Error armando la petición", Toast.LENGTH_SHORT).show();
            return;
        }

        // Evita enviar dos veces si se toca el botón repetido
        btnIngresar.setEnabled(false);

        ApiHttp.pedir("POST", "login.php", body.toString(),
                "application/json; charset=UTF-8",
                (codigo, cuerpo) -> {
                    if (isFinishing() || isDestroyed()) return;
                    btnIngresar.setEnabled(true);

                    if (codigo == 200) {
                        String nombre = "usuario";
                        try {
                            nombre = new JSONObject(cuerpo).optString("nombre", "usuario");
                        } catch (Exception ignorada) { }
                        Toast.makeText(this, "¡Bienvenido, " + nombre + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else if (codigo == -1) {
                        Toast.makeText(this, "No se pudo conectar al servidor", Toast.LENGTH_LONG).show();
                    } else {
                        String mensaje = "Credenciales inválidas";
                        try {
                            String api = new JSONObject(cuerpo).optString("error", "");
                            if (!api.isEmpty()) mensaje = api;
                        } catch (Exception ignorada) { }
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }
}