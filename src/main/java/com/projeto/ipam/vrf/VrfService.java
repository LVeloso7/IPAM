package com.projeto.ipam.vrf;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;


@Service 
@Transactional (readOnly = true)

public class VrfService {
    
    private final VrfRepository repository;
    
    public VrfService(VrfRepository repository){
        this.repository = repository;
    }
       
    public List<Vrf> listar() {
        return repository.findAll(Sort.by("nome"));
    }
    
    public Vrf buscarPorId(Long id){
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "VRF " + id + " não encontrada"));
    }
    
    @Transactional //1
    public Vrf criar (String nome, String rd, String descricao){
        String nomeLimpo = normalizar (nome);
        
        if (repository.existsByNomeIgnoreCase(nomeLimpo)){
            throw new ConflitoException("Já existe uma VRF com o nome '" + nomeLimpo + "'");
        }
        Vrf vrf = new Vrf(nomeLimpo, normalizar(rd), normalizar(descricao));
        return repository.save(vrf);
        
    }
    
    @Transactional //2
    public Vrf atualizar(Long id, String nome, String rd, String descricao){
        Vrf vrf = buscarPorId(id);
        String nomeLimpo = normalizar(nome);
        
        if (repository.existsByNomeIgnoreCaseAndIdNot(nomeLimpo, id)){
            throw new ConflitoException("Já existe outra VRF com o nome '" + nomeLimpo + "'");
        }
        
        vrf.atualizar(nomeLimpo, normalizar(rd), normalizar(descricao));
        return vrf;
    }
    
    @Transactional //3
    public void excluir(Long id) {
        Vrf vrf = buscarPorId(id);
        repository.delete(vrf);
    }
    
    private String normalizar (String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }
}