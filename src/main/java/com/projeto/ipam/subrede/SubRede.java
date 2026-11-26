package com.projeto.ipam.subrede;

import java.time.Instant;

import org.hibernate.annotations.ColumnTransformer;

import com.projeto.ipam.secao.Secao;
import com.projeto.ipam.vrf.Vrf;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sub_rede")
public class SubRede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vrf_id", nullable = false, updatable = false)
    private Vrf vrf;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secao_id", nullable = false)
    private Secao secao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pai_id")
    private SubRede pai;

    @Column(name = "cidr", nullable = false, updatable = false, columnDefinition = "cidr")
    @ColumnTransformer(write = "?::cidr")
    private String cidr;

    @Column(name = "vlan_id")
    private Long vlanId;

    @Column(name = "unidade", length = 100)
    private String unidade;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "criado_por", nullable = false, updatable = false, length = 100)
    private String criadoPor;

    protected SubRede() {
    }

        public SubRede(Vrf vrf, Secao secao, String cidr, Long vlanId, String unidade,
                   String descricao, String criadoPor) {
        this.vrf = vrf;
        this.secao = secao;
        this.cidr = cidr;
        this.vlanId = vlanId;
        this.unidade = unidade;
        this.descricao = descricao;
        this.criadoPor = criadoPor;
        this.criadoEm = Instant.now();
    }

    // VRF e CIDR são imutáveis; a seção, por ser organizacional, pode mudar.
    public void atualizarDados(Secao secao, Long vlanId, String unidade, String descricao) {
        this.secao = secao;
        this.vlanId = vlanId;
        this.unidade = unidade;
        this.descricao = descricao;
    }

    // Sem "public": só classes do pacote subrede (o Service) podem definir o pai.
    void definirPai(SubRede pai) {
        this.pai = pai;
    }

    public Secao getSecao() { return secao; }
    public Long getId() { return id; }
    public Vrf getVrf() { return vrf; }
    public SubRede getPai() { return pai; }
    public String getCidr() { return cidr; }
    public Long getVlanId() { return vlanId; }
    public String getUnidade() { return unidade; }
    public String getDescricao() { return descricao; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getCriadoPor() { return criadoPor; }
}