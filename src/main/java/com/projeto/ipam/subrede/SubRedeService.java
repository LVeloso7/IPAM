package com.projeto.ipam.subrede;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.EnderecoRede;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.comum.Textos;
import com.projeto.ipam.ip.EnderecoIpRepository;
import com.projeto.ipam.vrf.Vrf;
import com.projeto.ipam.vrf.VrfService;
import com.projeto.ipam.secao.Secao;
import com.projeto.ipam.secao.SecaoService;

@Service
@Transactional(readOnly = true)
public class SubRedeService {

    private final SubRedeRepository repository;
    private final EnderecoIpRepository ipRepository;
    private final VrfService vrfService;
    private final SecaoService secaoService;

    public SubRedeService(SubRedeRepository repository,
                          EnderecoIpRepository ipRepository,
                          VrfService vrfService,
                          SecaoService secaoService) {
        this.repository = repository;
        this.ipRepository = ipRepository;
        this.vrfService = vrfService;
        this.secaoService = secaoService;
    }
    
    public List<SubRedeResponse> listarPorSecao(Long secaoId) {
        secaoService.buscarPorId(secaoId);
        return repository.listarPorSecao(secaoId).stream()
                .map(SubRedeResponse::de)
                .toList();
    }

    public List<SubRedeResponse> listarPorVrf(Long vrfId) {
        vrfService.buscarPorId(vrfId);
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
    public SubRedeResponse criar(Long vrfId, Long secaoId, String cidr, Long vlanId,
                                 String unidade, String descricao, String usuario) {
        String cidrNormalizado = EnderecoRede.normalizarCidr(cidr);
        Secao secao = secaoService.buscarPorId(secaoId);
        Vrf vrf = vrfService.bloquearParaAlteracao(vrfId);

        if (repository.existeNaVrf(vrfId, cidrNormalizado)) {
            throw new ConflitoException("A sub-rede " + cidrNormalizado
                    + " já está cadastrada na VRF '" + vrf.getNome() + "'");
        }

        SubRede pai = repository.encontrarPai(vrfId, cidrNormalizado).orElse(null);

        // Um IP já cadastrado não pode virar rede ou broadcast da nova sub-rede.
        for (String reservado : EnderecoRede.enderecosReservados(cidrNormalizado)) {
            if (ipRepository.existeNaVrf(vrfId, reservado)) {
                throw new ConflitoException("O IP " + reservado + " já está cadastrado e seria"
                        + " o endereço de rede ou de broadcast de " + cidrNormalizado);
            }
        }

        SubRede nova = new SubRede(vrf, secao, cidrNormalizado, vlanId,
                Textos.normalizar(unidade), Textos.normalizar(descricao), usuario);
        nova.definirPai(pai);

        repository.save(nova);
        repository.adotarFilhas(vrfId, nova.getId(), cidrNormalizado);

        if (pai != null) {
            ipRepository.moverParaNovaSubRede(pai.getId(), nova.getId(), cidrNormalizado);
        }

        return buscarPorId(nova.getId());
    }

    @Transactional
    public SubRedeResponse atualizar(Long id, Long secaoId, Long vlanId,
                                     String unidade, String descricao) {
        SubRede subRede = carregar(id);
        Secao secao = secaoService.buscarPorId(secaoId);
        subRede.atualizarDados(secao, vlanId,
                Textos.normalizar(unidade), Textos.normalizar(descricao));
        return SubRedeResponse.de(subRede);
    }

    @Transactional
    public void excluir(Long id) {
        SubRede subRede = carregar(id);
        vrfService.bloquearParaAlteracao(subRede.getVrf().getId());

        if (ipRepository.existsBySubRedeId(id)) {
            throw new ConflitoException("A sub-rede " + subRede.getCidr()
                    + " possui endereços IP cadastrados e não pode ser excluída");
        }

        repository.promoverFilhas(id);
        repository.deleteById(id);
    }

    private SubRede carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sub-rede " + id + " não encontrada"));
    }
}