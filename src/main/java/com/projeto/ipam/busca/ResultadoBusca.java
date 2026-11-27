package com.projeto.ipam.busca;

import java.util.List;

import com.projeto.ipam.ip.EnderecoIpResponse;
import com.projeto.ipam.subrede.SubRedeResponse;

public record ResultadoBusca(
        String termo,
        String tipo,
        List<SubRedeResponse> subRedes,
        List<EnderecoIpResponse> enderecos) {
}