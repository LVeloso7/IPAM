package com.projeto.ipam.subrede;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubRedeRepository extends JpaRepository<SubRede, Long> {

    // Consulta derivada: alguma sub-rede usa esta VRF?
    boolean existsByVrfId(Long vrfId);

    // Já existe este CIDR nesta VRF?
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM sub_rede
                WHERE vrf_id = :vrfId
                  AND cidr = CAST(:cidr AS cidr))
            """, nativeQuery = true)
    boolean existeNaVrf(@Param("vrfId") Long vrfId, @Param("cidr") String cidr);

    // O pai é o menor bloco da mesma VRF que contém o CIDR informado.
    @Query(value = """
            SELECT * FROM sub_rede
            WHERE vrf_id = :vrfId
              AND cidr >> CAST(:cidr AS cidr)
            ORDER BY masklen(cidr) DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<SubRede> encontrarPai(@Param("vrfId") Long vrfId, @Param("cidr") String cidr);

    // Em qual rede (a mais específica) este IP está?
    @Query(value = """
            SELECT * FROM sub_rede
            WHERE vrf_id = :vrfId
              AND cidr >>= CAST(:ip AS inet)
            ORDER BY masklen(cidr) DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<SubRede> encontrarRedeDoIp(@Param("vrfId") Long vrfId, @Param("ip") String ip);

    // Todas as sub-redes de uma VRF, em ordem numérica de endereço.
    @Query(value = "SELECT * FROM sub_rede WHERE vrf_id = :vrfId ORDER BY cidr",
           nativeQuery = true)
    List<SubRede> listarPorVrf(@Param("vrfId") Long vrfId);

    // Filhas diretas de uma sub-rede, em ordem numérica.
    @Query(value = "SELECT * FROM sub_rede WHERE pai_id = :paiId ORDER BY cidr",
           nativeQuery = true)
    List<SubRede> listarFilhas(@Param("paiId") Long paiId);

    // Ao criar uma sub-rede, ela "adota" as que passou a conter.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE sub_rede AS filha
            SET pai_id = :novaId
            WHERE filha.vrf_id = :vrfId
              AND filha.cidr << CAST(:cidr AS cidr)
              AND (filha.pai_id IS NULL OR NOT EXISTS (
                    SELECT 1 FROM sub_rede AS atual
                    WHERE atual.id = filha.pai_id
                      AND atual.cidr << CAST(:cidr AS cidr)))
            """, nativeQuery = true)
    int adotarFilhas(@Param("vrfId") Long vrfId,
                     @Param("novaId") Long novaId,
                     @Param("cidr") String cidr);

    // Antes de excluir uma sub-rede, as filhas dela sobem para o avô.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE sub_rede
            SET pai_id = (SELECT pai_id FROM sub_rede WHERE id = :removidaId)
            WHERE pai_id = :removidaId
            """, nativeQuery = true)
    int promoverFilhas(@Param("removidaId") Long removidaId);

        boolean existsBySecaoId(Long secaoId);

    @Query(value = "SELECT * FROM sub_rede WHERE secao_id = :secaoId ORDER BY cidr",
           nativeQuery = true)
    List<SubRede> listarPorSecao(@Param("secaoId") Long secaoId);

        boolean existsByVlanId(Long vlanId);

    @Query(value = "SELECT * FROM sub_rede WHERE vlan_id = :vlanId ORDER BY cidr",
           nativeQuery = true)
    List<SubRede> listarPorVlan(@Param("vlanId") Long vlanId);

    @Query(value = """
            SELECT pai_id AS paiId, CAST(cidr AS text) AS cidr
            FROM sub_rede
            WHERE pai_id IN (:ids)
            """, nativeQuery = true)
    List<FilhaResumo> listarFilhasDe(@Param("ids") Collection<Long> ids);
    
}
