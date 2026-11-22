package com.example.presupuestador;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etPassword, etNroMatricula;
    private CheckBox chkMatricula;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        chkMatricula = findViewById(R.id.chkMatricula);
        etNroMatricula = findViewById(R.id.etNroMatricula);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView txtVolverLogin = findViewById(R.id.txtVolverLogin);

        chkMatricula.setOnCheckedChangeListener((boton, marcado) ->
                etNroMatricula.setVisibility(marcado ? View.VISIBLE : View.GONE));

        txtVolverLogin.setOnClickListener(v -> finish());
        btnRegistrar.setOnClickListener(v -> registrar());
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        boolean tieneMatricula = chkMatricula.isChecked();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresá tu nombre");
            etNombre.requestFocus();
            return;
        }
        if (apellido.isEmpty()) {
            etApellido.setError("Ingresá tu apellido");
            etApellido.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresá un email válido");
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 8 || password.length() > 72) {
            etPassword.setError("Entre 8 y 72 caracteres");
            etPassword.requestFocus();
            return;
        }

        int nroMatricula = 0;
        if (tieneMatricula) {
            try {
                nroMatricula = Integer.parseInt(etNroMatricula.getText().toString().trim());
                if (nroMatricula < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                etNroMatricula.setError("Ingresá un número de matrícula válido");
                etNroMatricula.requestFocus();
                return;
            }
        }

        JSONObject body = new JSONObject();
        try {
            body.put("nombre", nombre);
            body.put("apellido", apellido);
            body.put("email", email);
            body.put("password", password);
            body.put("matricula", tieneMatricula);
            if (tieneMatricula) {
                body.put("nro_matricula", nroMatricula);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error armando los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);   // evita enviar dos veces

        ApiHttp.pedir("POST", "registro.php", body.toString(),
                "application/json; charset=UTF-8",
                (codigo, cuerpo) -> {
                    if (isFinishing() || isDestroyed()) return;
                    btnRegistrar.setEnabled(true);

                    if (codigo == 201) {
                        Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                        // El servidor ya dejó la sesión iniciada
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else if (codigo == -1) {
                        Toast.makeText(this, "No se pudo conectar al servidor", Toast.LENGTH_LONG).show();
                    } else {
                        String mensaje = "Error (código " + codigo + ")";
                        try {
                            String api = new JSONObject(cuerpo).optString("error", "");
                            if (!api.isEmpty()) mensaje = api;
                        } catch (Exception ignorada) { }
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
