package com.projeto.ipam.vlan;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VlanRepository extends JpaRepository<Vlan, Long> {

    List<Vlan> findBySecaoIdOrderByNumeroAsc(Long secaoId);

    boolean existsBySecaoId(Long secaoId);

    boolean existsBySecaoIdAndNumero(Long secaoId, Integer numero);

    boolean existsBySecaoIdAndNumeroAndIdNot(Long secaoId, Integer numero, Long id);
}