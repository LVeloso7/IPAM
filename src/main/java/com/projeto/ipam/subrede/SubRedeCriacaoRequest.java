package com.projeto.ipam.subrede;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record SubRedeCriacaoRequest(

        @NotNull(message = "A seção é obrigatória")
        Long secaoId,

        @NotBlank(message = "O CIDR é obrigatório")
        @Size(max = 64, message = "O CIDR deve ter no máximo 64 caracteres")
        String cidr,

        Long vlanId,

        @Size(max = 100, message = "A unidade deve ter no máximo 100 caracteres")
        String unidade,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao
) {
}