package com.example.presupuestador;

public class Opcion {
    public int id;
    public String descripcion;

    public Opcion(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}