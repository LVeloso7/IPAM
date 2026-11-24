package com.projeto.ipam.ip;

final class Hostname {

    // Rótulos de 1 a 63 caracteres (letras, números e hífen, sem hífen nas pontas),
    // separados por ponto. Texto vazio é aceito: o Service o transforma em null.
    static final String REGEX =
            "^$|^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$";

    static final String MENSAGEM =
            "Hostname inválido: use letras, números, hífens e pontos (ex.: srv-erp.sinobras.local)";

    private Hostname() {
    }
}