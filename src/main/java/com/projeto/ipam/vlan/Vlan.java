package com.projeto.ipam.vlan;

import com.projeto.ipam.secao.Secao;

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
@Table(name = "vlan")
public class Vlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secao_id", nullable = false, updatable = false)
    private Secao secao;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    protected Vlan() {
    }

    public Vlan(Secao secao, Integer numero, String nome, String descricao) {
        this.secao = secao;
        this.numero = numero;
        this.nome = nome;
        this.descricao = descricao;
    }

    public void atualizar(Integer numero, String nome, String descricao) {
        this.numero = numero;
        this.nome = nome;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public Secao getSecao() { return secao; }
    public Integer getNumero() { return numero; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
}