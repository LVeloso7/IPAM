package com.projeto.ipam.vlan;

public record VlanResponse(Long id, Long secaoId, Integer numero, String nome, String descricao) {

    public static VlanResponse de(Vlan vlan) {
        return new VlanResponse(vlan.getId(), vlan.getSecao().getId(),
                vlan.getNumero(), vlan.getNome(), vlan.getDescricao());
    }
}