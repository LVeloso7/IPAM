package com.projeto.ipam.vlan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VlanRequest(

        @NotNull(message = "O número da VLAN é obrigatório")
        @Min(value = 1, message = "A VLAN deve estar entre 1 e 4094")
        @Max(value = 4094, message = "A VLAN deve estar entre 1 e 4094")
        Integer numero,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao
) {
}