package com.projeto.ipam.ip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoIpCriacaoRequest(

        @NotBlank(message = "O endereço é obrigatório")
        @Size(max = 45, message = "O endereço deve ter no máximo 45 caracteres")
        String endereco,

        @Size(max = 253, message = "O hostname deve ter no máximo 253 caracteres")
        @Pattern(regexp = Hostname.REGEX, message = Hostname.MENSAGEM)
        String hostname,

        @Size(max = 32, message = "O MAC deve ter no máximo 32 caracteres")
        String mac,

        StatusIp status,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao
) {
}