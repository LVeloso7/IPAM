package com.projeto.ipam.ip;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.DadoInvalidoException;
import com.projeto.ipam.comum.EnderecoRede;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.comum.Textos;
import com.projeto.ipam.subrede.SubRede;
import com.projeto.ipam.subrede.SubRedeRepository;
import com.projeto.ipam.vrf.VrfService;

@Service
@Transactional(readOnly = true)
public class EnderecoIpService {

    private final EnderecoIpRepository repository;
    private final SubRedeRepository subRedeRepository;
    private final VrfService vrfService;

    public EnderecoIpService(EnderecoIpRepository repository,
                             SubRedeRepository subRedeRepository,
                             VrfService vrfService) {
        this.repository = repository;
        this.subRedeRepository = subRedeRepository;
        this.vrfService = vrfService;
    }

    public List<EnderecoIpResponse> listarPorSubRede(Long subRedeId) {
        carregarSubRede(subRedeId);
        return repository.listarPorSubRede(subRedeId).stream()
                .map(EnderecoIpResponse::de)
                .toList();
    }

    public EnderecoIpResponse buscarPorId(Long id) {
        return EnderecoIpResponse.de(carregar(id));
    }

    public EnderecoIpResponse buscarPorEndereco(Long vrfId, String endereco) {
        vrfService.buscarPorId(vrfId);
        String ip = EnderecoRede.normalizarIp(endereco);

        return repository.buscarNaVrf(vrfId, ip)
                .map(EnderecoIpResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "O IP " + ip + " não está cadastrado nesta VRF"));
    }

    public List<EnderecoIpResponse> buscarPorHostname(String termo) {
        return repository.findTop100ByHostnameContainingIgnoreCaseOrderByHostnameAsc(termo.strip())
                .stream()
                .map(EnderecoIpResponse::de)
                .toList();
    }

    public List<EnderecoIpResponse> buscarEmTodasVrfs(String ip) {
        return repository.buscarEmTodasVrfs(ip).stream()
                .map(EnderecoIpResponse::de)
                .toList();
    }

    public List<EnderecoIpResponse> buscarPorPrefixo(String prefixo) {
        return repository.buscarPorPrefixo(prefixo).stream()
                .map(EnderecoIpResponse::de)
                .toList();
    }

    public ProximoIpResponse proximoLivre(Long subRedeId) {
        SubRede subRede = carregarSubRede(subRedeId);

        List<String> filhas = subRedeRepository.listarFilhas(subRedeId).stream()
                .map(SubRede::getCidr)
                .toList();
        List<String> usados = repository.listarEnderecosUsados(subRedeId);

        String livre = EnderecoRede.proximoLivre(subRede.getCidr(), filhas, usados)
                .orElseThrow(() -> new ConflitoException(
                        "A sub-rede " + subRede.getCidr() + " não tem endereços livres"));

        return new ProximoIpResponse(subRede.getId(), subRede.getCidr(), livre);
    }

    @Transactional
    public EnderecoIpResponse criar(Long vrfId, String endereco, String hostname, String mac,
                                    StatusIp status, String descricao, String usuario) {
        String ip = EnderecoRede.normalizarIp(endereco);
        String macNormalizado = EnderecoRede.normalizarMac(mac);
        vrfService.bloquearParaAlteracao(vrfId);

        SubRede subRede = subRedeRepository.encontrarRedeDoIp(vrfId, ip)
                .orElseThrow(() -> new ConflitoException("Nenhuma sub-rede desta VRF contém o IP "
                        + ip + ". Cadastre a sub-rede antes do endereço."));

        if (EnderecoRede.enderecosReservados(subRede.getCidr()).contains(ip)) {
            throw new DadoInvalidoException("O IP " + ip
                    + " é o endereço de rede ou de broadcast da sub-rede " + subRede.getCidr());
        }
        if (repository.existeNaVrf(vrfId, ip)) {
            throw new ConflitoException("O IP " + ip + " já está cadastrado nesta VRF");
        }

        EnderecoIp novo = new EnderecoIp(subRede, ip, normalizarHostname(hostname), macNormalizado,
                status != null ? status : StatusIp.EM_USO, Textos.normalizar(descricao), usuario);

        return EnderecoIpResponse.de(repository.save(novo));
    }

    @Transactional
    public EnderecoIpResponse atualizar(Long id, String hostname, String mac,
                                        StatusIp status, String descricao, String usuario) {
        EnderecoIp ip = carregar(id);
        ip.atualizar(normalizarHostname(hostname), EnderecoRede.normalizarMac(mac),
                status, Textos.normalizar(descricao), usuario);
        return EnderecoIpResponse.de(ip);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(carregar(id));
    }

    private EnderecoIp carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Endereço IP " + id + " não encontrado"));
    }

    private SubRede carregarSubRede(Long id) {
        return subRedeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sub-rede " + id + " não encontrada"));
    }

    // Hostnames não diferenciam maiúsculas: grava sempre em minúsculas.
    private String normalizarHostname(String hostname) {
        String limpo = Textos.normalizar(hostname);
        return limpo == null ? null : limpo.toLowerCase(Locale.ROOT);
    }
}