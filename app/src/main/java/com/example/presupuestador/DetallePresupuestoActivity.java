package com.example.presupuestador;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetallePresupuestoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_presupuesto";
    public static final String EXTRA_NRO = "nro";

    private TextView titulo, estado, cliente, total, mensaje;
    private LinearLayout contenedorItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_presupuesto);

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        String nro = getIntent().getStringExtra(EXTRA_NRO);

        titulo = findViewById(R.id.txtTituloDetalle);
        estado = findViewById(R.id.txtEstadoDetalle);
        cliente = findViewById(R.id.txtClienteDetalle);
        total = findViewById(R.id.txtTotalDetalle);
        mensaje = findViewById(R.id.txtMensajeDetalle);
        contenedorItems = findViewById(R.id.contenedorItemsDetalle);

        titulo.setText("Presupuesto " + nro);
        findViewById(R.id.btnVolverDetalle).setOnClickListener(v -> finish());

        if (id < 1) {
            mensaje.setText("Presupuesto inválido");
            return;
        }

        mensaje.setText("Cargando...");
        pedirDetalle(id);
    }

    private void pedirDetalle(int id) {
        ApiHttp.pedir("GET", "presupuestos.php?id=" + id, null, null, (codigo, cuerpo) -> {
            if (isFinishing() || isDestroyed()) return;
            Log.d("DETALLE", "GET codigo: " + codigo);

            if (codigo == 200) {
                try {
                    mostrarDetalle(new JSONObject(cuerpo));
                    mensaje.setText("");
                } catch (Exception e) {
                    mensaje.setText("Respuesta inválida del servidor");
                }
            } else {
                String texto;
                if (codigo == -1) {
                    texto = "Sin conexión con el servidor";
                } else {
                    texto = "Error (código " + codigo + ")";
                    try {
                        String api = new JSONObject(cuerpo).optString("error", "");
                        if (!api.isEmpty()) texto = api;
                    } catch (Exception ignorada) { }
                }
                mensaje.setText(texto);
                Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDetalle(JSONObject p) throws Exception {
        titulo.setText("Presupuesto " + p.optString("nro", "-"));
        estado.setText("Estado: " + p.optString("estado", "-"));

        cliente.setText("Cliente: " + p.optString("cliente_nombre", "") + " " + p.optString("cliente_apellido", "")
                + "\nTeléfono: " + p.optString("cliente_telefono", "-")
                + "\nEmail: " + p.optString("cliente_email", "-")
                + "\nDirección: " + p.optString("cliente_direccion", "-"));

        contenedorItems.removeAllViews();
        JSONArray items = p.optJSONArray("items");
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject it = items.getJSONObject(i);
                double cantidad = Double.parseDouble(it.optString("cantidad", "0"));
                double precio = Double.parseDouble(it.optString("precio_unitario", "0"));

                TextView fila = new TextView(this);
                fila.setPadding(0, 12, 0, 12);
                fila.setText(it.optString("trabajo", "-")
                        + "\n" + it.optString("categoria", "-")
                        + "\n" + formatear(cantidad) + " x $" + formatear(precio)
                        + " (" + it.optString("unidad", "-") + ")"
                        + " = $" + formatear(cantidad * precio));
                contenedorItems.addView(fila);
            }
        }

        total.setText("Total: $" + p.optString("total", "0"));
    }

    private String formatear(double valor) {
        return String.format(java.util.Locale.getDefault(), "%.2f", valor);
    }
}