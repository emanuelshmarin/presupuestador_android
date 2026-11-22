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
        setContentView(R.layout.activity_home); // <-- ESTO FALTABA

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