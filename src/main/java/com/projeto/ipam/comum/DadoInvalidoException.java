package com.projeto.ipam.comum;

public class DadoInvalidoException extends RuntimeException {

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}