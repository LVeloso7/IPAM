package com.projeto.ipam.secao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.comum.Textos;
import com.projeto.ipam.subrede.SubRedeRepository;
import com.projeto.ipam.vlan.VlanRepository;

@Service
@Transactional(readOnly = true)
public class SecaoService {

    private final SecaoRepository repository;
    private final SubRedeRepository subRedeRepository;
    private final VlanRepository vlanRepository;

    public SecaoService(SecaoRepository repository, SubRedeRepository subRedeRepository, VlanRepository vlanRepository) {
        this.repository = repository;
        this.subRedeRepository = subRedeRepository;
        this.vlanRepository = vlanRepository;
    }

    public List<Secao> listar() {
        return repository.findAll(Sort.by("nome"));
    }

    public Secao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Seção " + id + " não encontrada"));
    }

    @Transactional
    public Secao criar(String nome, String descricao) {
        String nomeLimpo = Textos.normalizar(nome);
        if (repository.existsByNomeIgnoreCase(nomeLimpo)) {
            throw new ConflitoException("Já existe uma seção com o nome '" + nomeLimpo + "'");
        }
        return repository.save(new Secao(nomeLimpo, Textos.normalizar(descricao)));
    }

    @Transactional
    public Secao atualizar(Long id, String nome, String descricao) {
        Secao secao = buscarPorId(id);
        String nomeLimpo = Textos.normalizar(nome);
        if (repository.existsByNomeIgnoreCaseAndIdNot(nomeLimpo, id)) {
            throw new ConflitoException("Já existe outra seção com o nome '" + nomeLimpo + "'");
        }
        secao.atualizar(nomeLimpo, Textos.normalizar(descricao));
        return secao;
    }

    @Transactional
    public void excluir(Long id) {
        Secao secao = buscarPorId(id);
        if (subRedeRepository.existsBySecaoId(id)) {
            throw new ConflitoException("A seção '" + secao.getNome()
                    + "' possui sub-redes e não pode ser excluída");
        }
        if (vlanRepository.existsBySecaoId(id)) {
            throw new ConflitoException("A seção '" + secao.getNome()
                    + "' possui VLANs e não pode ser excluída");
        }
        repository.delete(secao);
    }
}
