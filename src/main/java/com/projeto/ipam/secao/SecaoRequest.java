package com.projeto.ipam.secao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecaoRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao
) {
}