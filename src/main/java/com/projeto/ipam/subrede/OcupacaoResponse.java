package com.projeto.ipam.subrede;

import com.projeto.ipam.comum.EnderecoRede;

public record OcupacaoResponse(String total, String ocupados, double percentual) {

    static OcupacaoResponse de(EnderecoRede.Ocupacao ocupacao) {
        return new OcupacaoResponse(
                ocupacao.total().toString(),
                ocupacao.ocupados().toString(),
                ocupacao.percentual());
    }
}