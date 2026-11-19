package com.projeto.ipam.vrf;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name="vrf")

public class Vrf {    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;
    
    @Column (name = "rd", length = 50)
    private String rd;
    
    @Column (name = "descricao", columnDefinition = "text")
    private String descricao;
    
    //Construtor exigido pelo JPA. exigido para ninguem usar ele no código
    protected Vrf(){
    }
    
    //Construtor usado pelo código para criar uma nova VRF
    public Vrf (String nome, String rd, String descricao){
        this.nome = nome;
        this.rd = rd;
        this.descricao = descricao;
    }
    
    public void atualizar (String nome, String rd, String descricao){
        this.nome = nome;
        this.rd = rd;
        this.descricao = descricao;  
    }
    
    public Long getId(){
        return id;
    }
    
    public String getNome(){
        return nome;
    }
    
    public String getRd(){
        return rd;
    }
    
    public String getDescricao(){
        return descricao;
    } 
    
}