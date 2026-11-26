package com.projeto.ipam.subrede;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.ConflitoException;
import com.projeto.ipam.comum.DadoInvalidoException;
import com.projeto.ipam.comum.EnderecoRede;
import com.projeto.ipam.comum.RecursoNaoEncontradoException;
import com.projeto.ipam.comum.Textos;
import com.projeto.ipam.ip.ContagemPorSubRede;
import com.projeto.ipam.ip.EnderecoIpRepository;
import com.projeto.ipam.secao.Secao;
import com.projeto.ipam.secao.SecaoService;
import com.projeto.ipam.vlan.Vlan;
import com.projeto.ipam.vlan.VlanService;
import com.projeto.ipam.vrf.Vrf;
import com.projeto.ipam.vrf.VrfService;

@Service
@Transactional(readOnly = true)
public class SubRedeService {

    private final SubRedeRepository repository;
    private final EnderecoIpRepository ipRepository;
    private final VrfService vrfService;
    private final SecaoService secaoService;
    private final VlanService vlanService;

    public SubRedeService(SubRedeRepository repository,
                          EnderecoIpRepository ipRepository,
                          VrfService vrfService,
                          SecaoService secaoService,
                          VlanService vlanService) {
        this.repository = repository;
        this.ipRepository = ipRepository;
        this.vrfService = vrfService;
        this.secaoService = secaoService;
        this.vlanService = vlanService;
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    public List<SubRedeResponse> listarPorVrf(Long vrfId) {
        vrfService.buscarPorId(vrfId);
        return paraRespostas(repository.listarPorVrf(vrfId));
    }

    public List<SubRedeResponse> listarPorSecao(Long secaoId) {
        secaoService.buscarPorId(secaoId);
        return paraRespostas(repository.listarPorSecao(secaoId));
    }

    public List<SubRedeResponse> listarPorVlan(Long vlanId) {
        vlanService.buscarPorId(vlanId);
        return paraRespostas(repository.listarPorVlan(vlanId));
    }

    public List<SubRedeResponse> listarFilhas(Long id) {
        carregar(id);
        return paraRespostas(repository.listarFilhas(id));
    }

    public SubRedeResponse buscarPorId(Long id) {
        return paraResposta(carregar(id));
    }

    public SubRedeResponse buscarRedeDoIp(Long vrfId, String ip) {
        vrfService.buscarPorId(vrfId);
        String ipNormalizado = EnderecoRede.normalizarIp(ip);

        return repository.encontrarRedeDoIp(vrfId, ipNormalizado)
                .map(this::paraResposta)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma sub-rede cadastrada contém o IP " + ipNormalizado));
    }

    // As 20 sub-redes mais ocupadas acima do limite, da mais para a menos ocupada.
    public List<SubRedeResponse> listarCriticas(double limite) {
        return paraRespostas(repository.findAll()).stream()
                .filter(s -> s.ocupacao().percentual() >= limite)
                .sorted(Comparator.comparingDouble(
                        (SubRedeResponse s) -> s.ocupacao().percentual()).reversed())
                .limit(20)
                .toList();
    }

    // ---------------------------------------------------------------
    // Alterações
    // ---------------------------------------------------------------

    @Transactional
    public SubRedeResponse criar(Long vrfId, Long secaoId, String cidr, Long vlanId,
                                 String unidade, String descricao, String usuario) {
        String cidrNormalizado = EnderecoRede.normalizarCidr(cidr);
        Secao secao = secaoService.buscarPorId(secaoId);
        Vlan vlan = resolverVlan(vlanId, secao);
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

        SubRede nova = new SubRede(vrf, secao, cidrNormalizado, vlan,
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
        Vlan vlan = resolverVlan(vlanId, secao);

        subRede.atualizarDados(secao, vlan,
                Textos.normalizar(unidade), Textos.normalizar(descricao));
        return paraResposta(subRede);
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

    // ---------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------

    private SubRede carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sub-rede " + id + " não encontrada"));
    }

    // A VLAN precisa pertencer à mesma seção da sub-rede.
    private Vlan resolverVlan(Long vlanId, Secao secao) {
        if (vlanId == null) {
            return null;
        }
        Vlan vlan = vlanService.buscarPorId(vlanId);
        if (!vlan.getSecao().getId().equals(secao.getId())) {
            throw new DadoInvalidoException("A VLAN " + vlan.getNumero()
                    + " pertence a outra seção. Escolha uma VLAN da seção '" + secao.getNome() + "'");
        }
        return vlan;
    }

    // Converte uma lista de sub-redes em respostas com ocupação, com duas consultas no total.
    private List<SubRedeResponse> paraRespostas(List<SubRede> subRedes) {
        if (subRedes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = subRedes.stream().map(SubRede::getId).toList();

        Map<Long, Long> ipsPorSubRede = ipRepository.contarPorSubRedes(ids).stream()
                .collect(Collectors.toMap(
                        ContagemPorSubRede::getSubRedeId,
                        ContagemPorSubRede::getQuantidade));

        Map<Long, List<String>> filhasPorPai = repository.listarFilhasDe(ids).stream()
                .collect(Collectors.groupingBy(
                        FilhaResumo::getPaiId,
                        Collectors.mapping(FilhaResumo::getCidr, Collectors.toList())));

        return subRedes.stream()
                .map(s -> SubRedeResponse.de(s, EnderecoRede.calcularOcupacao(
                        s.getCidr(),
                        filhasPorPai.getOrDefault(s.getId(), List.of()),
                        ipsPorSubRede.getOrDefault(s.getId(), 0L))))
                .toList();
    }

    // Atalho para converter uma única sub-rede.
    private SubRedeResponse paraResposta(SubRede subRede) {
        return paraRespostas(List.of(subRede)).get(0);
    }
}