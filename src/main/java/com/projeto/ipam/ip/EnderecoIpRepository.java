package com.projeto.ipam.ip;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnderecoIpRepository extends JpaRepository<EnderecoIp, Long> {

    // Consultas derivadas

    boolean existsBySubRedeId(Long subRedeId);

    long countBySubRedeId(Long subRedeId);

    List<EnderecoIp> findTop100ByHostnameContainingIgnoreCaseOrderByHostnameAsc(String termo);

    // Consultas nativas

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM endereco_ip
                WHERE vrf_id = :vrfId
                  AND endereco = CAST(:ip AS inet))
            """, nativeQuery = true)
    boolean existeNaVrf(@Param("vrfId") Long vrfId, @Param("ip") String ip);

    @Query(value = """
            SELECT * FROM endereco_ip
            WHERE vrf_id = :vrfId
              AND endereco = CAST(:ip AS inet)
            """, nativeQuery = true)
    Optional<EnderecoIp> buscarNaVrf(@Param("vrfId") Long vrfId, @Param("ip") String ip);

    @Query(value = """
            SELECT * FROM endereco_ip
            WHERE sub_rede_id = :subRedeId
            ORDER BY endereco
            """, nativeQuery = true)
    List<EnderecoIp> listarPorSubRede(@Param("subRedeId") Long subRedeId);

    // Só os endereços, como texto, para o cálculo do próximo livre.
    @Query(value = "SELECT host(endereco) FROM endereco_ip WHERE sub_rede_id = :subRedeId",
           nativeQuery = true)
    List<String> listarEnderecosUsados(@Param("subRedeId") Long subRedeId);

    // Ao criar uma sub-rede, os IPs do pai que caem nela descem para a nova.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE endereco_ip
            SET sub_rede_id = :novaId
            WHERE sub_rede_id = :paiId
              AND endereco <<= CAST(:cidr AS cidr)
            """, nativeQuery = true)
    int moverParaNovaSubRede(@Param("paiId") Long paiId,
                             @Param("novaId") Long novaId,
                             @Param("cidr") String cidr);


    @Query(value = """
            SELECT sub_rede_id AS subRedeId, COUNT(*) AS quantidade
            FROM endereco_ip
            WHERE sub_rede_id IN (:ids)
            GROUP BY sub_rede_id
            """, nativeQuery = true)
    List<ContagemPorSubRede> contarPorSubRedes(@Param("ids") Collection<Long> ids);

    @Query(value = """
            SELECT * FROM endereco_ip
            WHERE endereco = CAST(:ip AS inet)
            ORDER BY vrf_id
            """, nativeQuery = true)
    List<EnderecoIp> buscarEmTodasVrfs(@Param("ip") String ip);

    @Query(value = """
            SELECT * FROM endereco_ip
            WHERE host(endereco) LIKE :prefixo || '%'
            ORDER BY endereco
            LIMIT 100
            """, nativeQuery = true)
    List<EnderecoIp> buscarPorPrefixo(@Param("prefixo") String prefixo);

}