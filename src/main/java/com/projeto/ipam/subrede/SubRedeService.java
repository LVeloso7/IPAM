package com.projeto.ipam.subrede;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.EnderecoRede;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.vrf.Vrf;
import com.projeto.ipam.vrf.VrfService;

@Service
@Transactional(readOnly = true)
public class SubRedeService {

    private final SubRedeRepository repository;
    private final VrfService vrfService;

    public SubRedeService(SubRedeRepository repository, VrfService vrfService) {
        this.repository = repository;
        this.vrfService = vrfService;
    }

    public List<SubRedeResponse> listarPorVrf(Long vrfId) {
        vrfService.buscarPorId(vrfId); // garante 404 se a VRF não existir
        return repository.listarPorVrf(vrfId).stream()
                .map(SubRedeResponse::de)
                .toList();
    }

    public SubRedeResponse buscarPorId(Long id) {
        return SubRedeResponse.de(carregar(id));
    }

    public List<SubRedeResponse> listarFilhas(Long id) {
        carregar(id);
        return repository.listarFilhas(id).stream()
                .map(SubRedeResponse::de)
                .toList();
    }

    public SubRedeResponse buscarRedeDoIp(Long vrfId, String ip) {
        vrfService.buscarPorId(vrfId);
        String ipNormalizado = EnderecoRede.normalizarIp(ip);

        return repository.encontrarRedeDoIp(vrfId, ipNormalizado)
                .map(SubRedeResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma sub-rede cadastrada contém o IP " + ipNormalizado));
    }

    @Transactional
    public SubRedeResponse criar(Long vrfId, String cidr, Long vlanId,
                                 String unidade, String descricao, String usuario) {
        String cidrNormalizado = EnderecoRede.normalizarCidr(cidr);
        Vrf vrf = vrfService.bloquearParaAlteracao(vrfId);

        if (repository.existeNaVrf(vrfId, cidrNormalizado)) {
            throw new ConflitoException("A sub-rede " + cidrNormalizado
                    + " já está cadastrada na VRF '" + vrf.getNome() + "'");
        }

        SubRede nova = new SubRede(vrf, cidrNormalizado, vlanId,
                normalizar(unidade), normalizar(descricao), usuario);
        repository.encontrarPai(vrfId, cidrNormalizado).ifPresent(nova::definirPai);

        repository.save(nova);
        repository.adotarFilhas(vrfId, nova.getId(), cidrNormalizado);

        return buscarPorId(nova.getId());
    }

    @Transactional
    public SubRedeResponse atualizar(Long id, Long vlanId, String unidade, String descricao) {
        SubRede subRede = carregar(id);
        subRede.atualizarDados(vlanId, normalizar(unidade), normalizar(descricao));
        return SubRedeResponse.de(subRede);
    }

    @Transactional
    public void excluir(Long id) {
        SubRede subRede = carregar(id);
        vrfService.bloquearParaAlteracao(subRede.getVrf().getId());

        repository.promoverFilhas(id);
        repository.deleteById(id);
    }

    private SubRede carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sub-rede " + id + " não encontrada"));
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.strip();
        return limpo.isEmpty() ? null : limpo;
    }
}