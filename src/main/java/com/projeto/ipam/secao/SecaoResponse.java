package com.projeto.ipam.secao;

public record SecaoResponse(Long id, String nome, String descricao) {

    public static SecaoResponse de(Secao secao) {
        return new SecaoResponse(secao.getId(), secao.getNome(), secao.getDescricao());
    }
}