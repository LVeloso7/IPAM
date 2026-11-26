package com.projeto.ipam.vrf;

import com.projeto.ipam.comum.Textos;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.subrede.SubRedeRepository;

@Service
@Transactional(readOnly = true)
public class VrfService {

    private final VrfRepository repository;
    private final SubRedeRepository subRedeRepository;

    public VrfService(VrfRepository repository, SubRedeRepository subRedeRepository) {
        this.repository = repository;
        this.subRedeRepository = subRedeRepository;
    }

    public List<Vrf> listar() {
        return repository.findAll(Sort.by(Vrf::getNome));
    }

    public Vrf buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> naoEncontrada(id));
    }

    // Bloqueia a VRF até o fim da transação: alterações estruturais em fila.
    @Transactional
    public Vrf bloquearParaAlteracao(Long id) {
        return repository.buscarComBloqueio(id)
                .orElseThrow(() -> naoEncontrada(id));
    }

    @Transactional
    public Vrf criar(String nome, String rd, String descricao) {
        String nomeLimpo = Textos.normalizar(nome);

        if (repository.existsByNomeIgnoreCase(nomeLimpo)) {
            throw new ConflitoException("Já existe uma VRF com o nome '" + nomeLimpo + "'");
        }

        Vrf vrf = new Vrf(nomeLimpo, Textos.normalizar(rd), Textos.normalizar(descricao));
        return repository.save(vrf);
    }

    @Transactional
    public Vrf atualizar(Long id, String nome, String rd, String descricao) {
        Vrf vrf = buscarPorId(id);
        String nomeLimpo = Textos.normalizar(nome);

        if (repository.existsByNomeIgnoreCaseAndIdNot(nomeLimpo, id)) {
            throw new ConflitoException("Já existe outra VRF com o nome '" + nomeLimpo + "'");
        }

        vrf.atualizar(nomeLimpo, Textos.normalizar(rd), Textos.normalizar(descricao));
        return vrf;
    }

    @Transactional
    public void excluir(Long id) {
        Vrf vrf = bloquearParaAlteracao(id);

        if (subRedeRepository.existsByVrfId(id)) {
            throw new ConflitoException(
                    "A VRF '" + vrf.getNome() + "' possui sub-redes e não pode ser excluída");
        }
        repository.delete(vrf);
    }

    private RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("VRF " + id + " não encontrada");
    }

}