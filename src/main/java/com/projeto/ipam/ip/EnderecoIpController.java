package com.projeto.ipam.ip;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.projeto.ipam.comum.DadoInvalidoException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class EnderecoIpController {

    private final EnderecoIpService service;

    public EnderecoIpController(EnderecoIpService service) {
        this.service = service;
    }

    @GetMapping("/sub-redes/{subRedeId}/ips")
    public List<EnderecoIpResponse> listarPorSubRede(@PathVariable Long subRedeId) {
        return service.listarPorSubRede(subRedeId);
    }

    @GetMapping("/sub-redes/{subRedeId}/ips/proximo-livre")
    public ProximoIpResponse proximoLivre(@PathVariable Long subRedeId) {
        return service.proximoLivre(subRedeId);
    }

    @PostMapping("/vrfs/{vrfId}/ips")
    public ResponseEntity<EnderecoIpResponse> criar(@PathVariable Long vrfId,
                                                    @Valid @RequestBody EnderecoIpCriacaoRequest req,
                                                    Authentication autenticacao,
                                                    UriComponentsBuilder uriBuilder) {
        EnderecoIpResponse criado = service.criar(vrfId, req.endereco(), req.hostname(),
                req.mac(), req.status(), req.descricao(), autenticacao.getName());

        URI local = uriBuilder.path("/api/ips/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @GetMapping("/vrfs/{vrfId}/ips/busca")
    public EnderecoIpResponse buscarPorEndereco(@PathVariable Long vrfId,
                                                @RequestParam String endereco) {
        return service.buscarPorEndereco(vrfId, endereco);
    }

    @GetMapping("/ips/busca")
    public List<EnderecoIpResponse> buscarPorHostname(@RequestParam String hostname) {
        String termo = hostname.strip();
        if (termo.length() < 2 || termo.length() > 100) {
            throw new DadoInvalidoException("A busca por hostname exige entre 2 e 100 caracteres");
        }
        return service.buscarPorHostname(termo);
    }

    @GetMapping("/ips/{id}")
    public EnderecoIpResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/ips/{id}")
    public EnderecoIpResponse atualizar(@PathVariable Long id,
                                        @Valid @RequestBody EnderecoIpAtualizacaoRequest req,
                                        Authentication autenticacao) {
        return service.atualizar(id, req.hostname(), req.mac(), req.status(),
                req.descricao(), autenticacao.getName());
    }

    @DeleteMapping("/ips/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}