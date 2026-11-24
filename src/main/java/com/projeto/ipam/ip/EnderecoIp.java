package com.projeto.ipam.ip;

import java.time.Instant;

import org.hibernate.annotations.ColumnTransformer;

import com.projeto.ipam.subrede.SubRede;
import com.projeto.ipam.vrf.Vrf;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "endereco_ip")
public class EnderecoIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vrf_id", nullable = false, updatable = false)
    private Vrf vrf;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_rede_id", nullable = false)
    private SubRede subRede;

    @Column(name = "endereco", nullable = false, updatable = false, columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String endereco;

    @Column(name = "hostname", length = 255)
    private String hostname;

    @Column(name = "mac", columnDefinition = "macaddr")
    @ColumnTransformer(write = "?::macaddr")
    private String mac;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusIp status;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "criado_por", nullable = false, updatable = false, length = 100)
    private String criadoPor;

    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    @Column(name = "atualizado_por", length = 100)
    private String atualizadoPor;

    protected EnderecoIp() {
    }

    public EnderecoIp(SubRede subRede, String endereco, String hostname, String mac,
                      StatusIp status, String descricao, String usuario) {
        this.vrf = subRede.getVrf();
        this.subRede = subRede;
        this.endereco = endereco;
        this.hostname = hostname;
        this.mac = mac;
        this.status = status;
        this.descricao = descricao;
        this.criadoPor = usuario;
        this.criadoEm = Instant.now();
    }

    public void atualizar(String hostname, String mac, StatusIp status,
                          String descricao, String usuario) {
        this.hostname = hostname;
        this.mac = mac;
        this.status = status;
        this.descricao = descricao;
        this.atualizadoPor = usuario;
        this.atualizadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public Vrf getVrf() { return vrf; }
    public SubRede getSubRede() { return subRede; }
    public String getEndereco() { return endereco; }
    public String getHostname() { return hostname; }
    public String getMac() { return mac; }
    public StatusIp getStatus() { return status; }
    public String getDescricao() { return descricao; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getCriadoPor() { return criadoPor; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public String getAtualizadoPor() { return atualizadoPor; }
}