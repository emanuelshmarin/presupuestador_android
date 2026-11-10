package com.example.presupuestador;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RegistroActivity extends AppCompatActivity {

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

        TextView txtVolverLogin = findViewById(R.id.txtVolverLogin);
        Button btnRegistrarse = findViewById(R.id.btnRegistrar);
        TextView txtNombre = findViewById(R.id.etNombre);
        TextView txtEmail = findViewById(R.id.etEmail);
        TextView txtPassword = findViewById(R.id.etPassword);

        txtVolverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoNombre = txtNombre.getText().toString();
                String textoEmail = txtEmail.getText().toString();
                String textoPassword = txtPassword.getText().toString();
                Toast.makeText(RegistroActivity.this,
                        "Nombre: " + textoNombre + "\nEmail: " + textoEmail + "\nPassword: " + textoPassword,
                        Toast.LENGTH_LONG).show();
            }
        });
    }


}
