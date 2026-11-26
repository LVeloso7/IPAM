package com.projeto.ipam.vlan;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.comum.Textos;
import com.projeto.ipam.secao.Secao;
import com.projeto.ipam.secao.SecaoService;
import com.projeto.ipam.subrede.SubRedeRepository;

@Service
@Transactional(readOnly = true)
public class VlanService {

    private final VlanRepository repository;
    private final SecaoService secaoService;
    private final SubRedeRepository subRedeRepository;

    public VlanService(VlanRepository repository, SecaoService secaoService,
                       SubRedeRepository subRedeRepository) {
        this.repository = repository;
        this.secaoService = secaoService;
        this.subRedeRepository = subRedeRepository;
    }

    public List<Vlan> listarPorSecao(Long secaoId) {
        secaoService.buscarPorId(secaoId);
        return repository.findBySecaoIdOrderByNumeroAsc(secaoId);
    }

    public Vlan buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("VLAN " + id + " não encontrada"));
    }

    @Transactional
    public Vlan criar(Long secaoId, Integer numero, String nome, String descricao) {
        Secao secao = secaoService.buscarPorId(secaoId);
        if (repository.existsBySecaoIdAndNumero(secaoId, numero)) {
            throw new ConflitoException("A VLAN " + numero
                    + " já está cadastrada na seção '" + secao.getNome() + "'");
        }
        return repository.save(new Vlan(secao, numero,
                Textos.normalizar(nome), Textos.normalizar(descricao)));
    }

    @Transactional
    public Vlan atualizar(Long id, Integer numero, String nome, String descricao) {
        Vlan vlan = buscarPorId(id);
        Long secaoId = vlan.getSecao().getId();
        if (repository.existsBySecaoIdAndNumeroAndIdNot(secaoId, numero, id)) {
            throw new ConflitoException("Já existe outra VLAN " + numero + " nesta seção");
        }
        vlan.atualizar(numero, Textos.normalizar(nome), Textos.normalizar(descricao));
        return vlan;
    }

    @Transactional
    public void excluir(Long id) {
        Vlan vlan = buscarPorId(id);
        if (subRedeRepository.existsByVlanId(id)) {
            throw new ConflitoException("A VLAN " + vlan.getNumero()
                    + " está em uso por sub-redes e não pode ser excluída");
        }
        repository.delete(vlan);
    }
}