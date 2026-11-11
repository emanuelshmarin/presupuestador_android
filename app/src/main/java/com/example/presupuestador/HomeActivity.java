package com.example.presupuestador;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Presupuestador");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Al arrancar, mostramos Presupuestos por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedor_fragment, new PresupuestoFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;

            if (id == R.id.nav_inicio) {
                // Todavía no existe InicioFragment, lo creamos después
                fragment = null;
            } else if (id == R.id.nav_presupuestos) {
                fragment = new PresupuestoFragment();
            } else if (id == R.id.nav_clientes) {
            fragment = new ClientesFragment();
            } else if (id == R.id.nav_precios) {
                fragment = new PreciosFragment();
            } else if (id == R.id.nav_configuracion) {
                fragment = null; // después
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedor_fragment, fragment)
                        .commit();
                return true;
            } else {
                return false; // por ahora no hace nada en las que faltan
            }
        });
    }
}