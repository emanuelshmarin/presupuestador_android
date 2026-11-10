package com.example.presupuestador;

public class LoginResponse {
    public int id;
    public String nombre;
    public String apellido;
    public String email;
    public String telefono;
    public String error;

    public boolean esExitoso() {
        return error == null || error.isEmpty();
    }
}
