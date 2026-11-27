package com.projeto.ipam.ip;

import java.time.Instant;

public record EnderecoIpResponse(
        Long id,
        Long vrfId,
        String vrfNome,
        Long subRedeId,
        String subRedeCidr,
        String endereco,
        String hostname,
        String mac,
        StatusIp status,
        String descricao,
        Instant criadoEm,
        String criadoPor,
        Instant atualizadoEm,
        String atualizadoPor) {

    static EnderecoIpResponse de(EnderecoIp ip) {
        return new EnderecoIpResponse(
                ip.getId(),
                ip.getVrf().getId(),
                ip.getVrf().getNome(),
                ip.getSubRede().getId(),
                ip.getSubRede().getCidr(),
                ip.getEndereco(),
                ip.getHostname(),
                ip.getMac(),
                ip.getStatus(),
                ip.getDescricao(),
                ip.getCriadoEm(),
                ip.getCriadoPor(),
                ip.getAtualizadoEm(),
                ip.getAtualizadoPor());
    }
}