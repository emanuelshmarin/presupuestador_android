package com.example.presupuestador;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class ApiClient {

    public static final String BASE_URL = "http://192.168.56.1/presupuestador/api/";

    private static ApiClient instancia;
    private RequestQueue cola;
    private final Context contexto;

    private ApiClient(Context contexto) {
        this.contexto = contexto.getApplicationContext();

        // 🔑 Esto hace que Volley guarde y envíe cookies automáticamente
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        this.cola = Volley.newRequestQueue(this.contexto);
    }

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