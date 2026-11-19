package com.projeto.ipam.vrf;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VrfRepository extends JpaRepository <Vrf, Long> {

    Optional<Vrf> findByNomeIgnoreCase(String nome);
    
    boolean existsByNomeIgnoreCase (String nome);
    
    boolean existsByNomeIgnoreCaseAndIdNot (String nome, Long id);
    
}