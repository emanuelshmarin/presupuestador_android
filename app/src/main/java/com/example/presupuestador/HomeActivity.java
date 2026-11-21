package com.example.presupuestador;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // <-- ESTO FALTABA
        toolbar.inflateMenu(R.menu.menu_arriba);
        toolbar.setOnMenuItemClickListener(item -> {
            Toast.makeText(this, "CLIC EN MENU", Toast.LENGTH_SHORT).show(); // <-- DIAGNÓSTICO
            if (item.getItemId() == R.id.action_cerrar_sesion) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Cerrar sesión")
                        .setMessage("¿Estás seguro que querés cerrar sesión?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            }
            return false;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedor_fragment, new PresupuestoFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;
            if (id == R.id.nav_presupuestos) {
                fragment = new PresupuestoFragment();
            } else if (id == R.id.nav_clientes) {
                fragment = new ClientesFragment();
            } else if (id == R.id.nav_precios) {
                fragment = new PreciosFragment();
            } else if (id == R.id.nav_configuracion) {
                fragment = new ConfiguracionFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedor_fragment, fragment)
                        .commit();
                return true;
            } else {
                return false;
            }
        });
    }
}