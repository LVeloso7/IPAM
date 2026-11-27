package com.projeto.ipam.painel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.ip.EnderecoIpRepository;
import com.projeto.ipam.secao.SecaoRepository;
import com.projeto.ipam.subrede.SubRedeRepository;
import com.projeto.ipam.vlan.VlanRepository;
import com.projeto.ipam.vrf.VrfRepository;

@Service
@Transactional(readOnly = true)
public class PainelService {

    private final VrfRepository vrfRepository;
    private final SecaoRepository secaoRepository;
    private final SubRedeRepository subRedeRepository;
    private final VlanRepository vlanRepository;
    private final EnderecoIpRepository enderecoIpRepository;

    public PainelService(VrfRepository vrfRepository, SecaoRepository secaoRepository,
                         SubRedeRepository subRedeRepository, VlanRepository vlanRepository,
                         EnderecoIpRepository enderecoIpRepository) {
        this.vrfRepository = vrfRepository;
        this.secaoRepository = secaoRepository;
        this.subRedeRepository = subRedeRepository;
        this.vlanRepository = vlanRepository;
        this.enderecoIpRepository = enderecoIpRepository;
    }

    public ResumoResponse resumo() {
        return new ResumoResponse(
                vrfRepository.count(),
                secaoRepository.count(),
                subRedeRepository.count(),
                vlanRepository.count(),
                enderecoIpRepository.count());
    }
}