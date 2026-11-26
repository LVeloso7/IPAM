package com.projeto.ipam.subrede;

import com.projeto.ipam.comum.EnderecoRede;

import java.time.Instant;

public record SubRedeResponse(
        Long id,
        Long vrfId,
        String vrfNome,
        Long secaoId,
        String secaoNome,
        String cidr,
        Long paiId,
        Long vlanId,
        Integer vlanNumero,
        String vlanNome,
        String unidade,
        String descricao,
        Instant criadoEm,
        String criadoPor,
        OcupacaoResponse ocupacao) {

    static SubRedeResponse de(SubRede s, EnderecoRede.Ocupacao ocupacao) {
        return new SubRedeResponse(
                s.getId(),
                s.getVrf().getId(),
                s.getVrf().getNome(),
                s.getSecao().getId(),
                s.getSecao().getNome(),
                s.getCidr(),
                s.getPai() != null ? s.getPai().getId() : null,
                s.getVlan() != null ? s.getVlan().getId() : null,
                s.getVlan() != null ? s.getVlan().getNumero() : null,
                s.getVlan() != null ? s.getVlan().getNome() : null,
                s.getUnidade(),
                s.getDescricao(),
                s.getCriadoEm(),
                s.getCriadoPor(),
                OcupacaoResponse.de(ocupacao));
    }
}