package com.projeto.ipam.vrf;

public record VrfResponse(Long id, String nome, String rd, String descricao) {

    public static VrfResponse de(Vrf vrf) {
        return new VrfResponse(vrf.getId(), vrf.getNome(), vrf.getRd(), vrf.getDescricao());
    }
}