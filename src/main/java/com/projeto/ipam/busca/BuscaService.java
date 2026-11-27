package com.projeto.ipam.busca;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.ipam.comum.DadoInvalidoException;
import com.projeto.ipam.comum.EnderecoRede;
import com.projeto.ipam.ip.EnderecoIpService;
import com.projeto.ipam.subrede.SubRedeService;

@Service
@Transactional(readOnly = true)
public class BuscaService {

    // Listas de permissão: só dígitos, pontos, hexadecimais e dois-pontos.
    // Nenhum curinga do LIKE (% ou _) consegue passar por elas.
    private static final Pattern PREFIXO_IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{0,3}){1,3}$");
    private static final Pattern PREFIXO_IPV6 = Pattern.compile("^[0-9a-fA-F]{0,4}(:[0-9a-fA-F]{0,4}){1,7}$");

    private final SubRedeService subRedeService;
    private final EnderecoIpService enderecoIpService;

    public BuscaService(SubRedeService subRedeService, EnderecoIpService enderecoIpService) {
        this.subRedeService = subRedeService;
        this.enderecoIpService = enderecoIpService;
    }

    public ResultadoBusca buscar(String texto) {
        String termo = texto == null ? "" : texto.strip();
        if (termo.length() < 2 || termo.length() > 100) {
            throw new DadoInvalidoException("A busca exige entre 2 e 100 caracteres");
        }

        Optional<String> ip = EnderecoRede.tentarNormalizarIp(termo);
        if (ip.isPresent()) {
            return new ResultadoBusca(termo, "IP",
                    subRedeService.buscarQueContem(ip.get()),
                    enderecoIpService.buscarEmTodasVrfs(ip.get()));
        }

        Optional<String> cidr = EnderecoRede.tentarNormalizarCidr(termo);
        if (cidr.isPresent()) {
            return new ResultadoBusca(termo, "CIDR",
                    subRedeService.buscarContidasEm(cidr.get()),
                    List.of());
        }

        if (PREFIXO_IPV4.matcher(termo).matches() || PREFIXO_IPV6.matcher(termo).matches()) {
            String prefixo = termo.toLowerCase();
            return new ResultadoBusca(termo, "PREFIXO",
                    subRedeService.buscarPorPrefixo(prefixo),
                    enderecoIpService.buscarPorPrefixo(prefixo));
        }

        return new ResultadoBusca(termo, "TEXTO",
                subRedeService.buscarPorDescricao(termo),
                enderecoIpService.buscarPorHostname(termo));
    }
}