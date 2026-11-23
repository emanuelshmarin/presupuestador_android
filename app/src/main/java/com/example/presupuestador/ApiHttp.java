package com.example.presupuestador;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiHttp {

    public interface Callback {
        // codigo = -1 si no hubo conexión (modo avión, API caída)
        void alTerminar(int codigo, String cuerpo);
    }

    private static final ExecutorService hilo = Executors.newSingleThreadExecutor();
    private static final Handler principal = new Handler(Looper.getMainLooper());

    static {
        java.net.CookieManager cookieManager = new java.net.CookieManager();
        cookieManager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(cookieManager);
    }

    public static void pedir(String metodo, String ruta, String body,
                             String contentType, Callback callback) {
        hilo.execute(() -> {
            int codigo = -1;
            String cuerpo = "";
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(ApiClient.BASE_URL + ruta).openConnection();
                con.setRequestMethod(metodo);
                con.setConnectTimeout(10000);
                con.setReadTimeout(10000);

                if (body != null) {
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", contentType);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }

                codigo = con.getResponseCode();
                InputStream is = (codigo >= 400) ? con.getErrorStream() : con.getInputStream();
                cuerpo = leer(is);
            } catch (Exception e) {
                codigo = -1;
                cuerpo = "";
            } finally {
                if (con != null) con.disconnect();
            }

            final int c = codigo;
            final String b = cuerpo;
            principal.post(() -> callback.alTerminar(c, b));
        });
    }

    private static String leer(InputStream is) throws Exception {
        if (is == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) out.write(buf, 0, n);
        is.close();
        return out.toString("UTF-8");
    }
}