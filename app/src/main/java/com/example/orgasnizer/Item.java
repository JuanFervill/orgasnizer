package com.example.orgasnizer;

public class Item {
    String fecha;
    String tipo;

    public Item(String fecha, String tipo) {
        this.fecha = fecha;
        this.tipo = tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
