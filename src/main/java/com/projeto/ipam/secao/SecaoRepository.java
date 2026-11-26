package com.projeto.ipam.secao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecaoRepository extends JpaRepository<Secao, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}