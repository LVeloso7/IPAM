package com.projeto.ipam.comum;

public final class Textos {

    private Textos() {
    }

    // Remove espaços nas pontas e transforma texto vazio em null.
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }
}