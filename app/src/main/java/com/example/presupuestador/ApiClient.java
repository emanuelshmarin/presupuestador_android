package com.example.presupuestador;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiClient {

    // 🔑 URL base: 10.0.2.2 = localhost de la PC desde el emulador
    public static final String BASE_URL = "http://192.168.56.1/presupuestador/api/";

    // Una sola instancia para toda la app
    private static ApiClient instancia;
    private RequestQueue cola;
    private final Context contexto;

    private ApiClient(Context contexto) {
        this.contexto = contexto.getApplicationContext();
        this.cola = Volley.newRequestQueue(this.contexto);
    }

    // Obtener la instancia (crear si no existe)
    public static synchronized ApiClient getInstance(Context contexto) {
        if (instancia == null) {
            instancia = new ApiClient(contexto);
        }
        return instancia;
    }

    public RequestQueue getCola() {
        return cola;
    }
}