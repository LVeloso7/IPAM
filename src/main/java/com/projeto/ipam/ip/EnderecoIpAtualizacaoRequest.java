package com.projeto.ipam.ip;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoIpAtualizacaoRequest(

        @Size(max = 253, message = "O hostname deve ter no máximo 253 caracteres")
        @Pattern(regexp = Hostname.REGEX, message = Hostname.MENSAGEM)
        String hostname,

        @Size(max = 32, message = "O MAC deve ter no máximo 32 caracteres")
        String mac,

        @NotNull(message = "O status é obrigatório")
        StatusIp status,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao
) {
}