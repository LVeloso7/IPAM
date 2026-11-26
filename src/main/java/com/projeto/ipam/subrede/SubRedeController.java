package com.projeto.ipam.subrede;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SubRedeController {

    private final SubRedeService service;

    public SubRedeController(SubRedeService service) {
        this.service = service;
    }

    @GetMapping("/vrfs/{vrfId}/sub-redes")
    public List<SubRedeResponse> listarPorVrf(@PathVariable Long vrfId) {
        return service.listarPorVrf(vrfId);
    }

    @GetMapping("/vrfs/{vrfId}/sub-redes/busca")
    public SubRedeResponse buscarPorIp(@PathVariable Long vrfId, @RequestParam String ip) {
        return service.buscarRedeDoIp(vrfId, ip);
    }

    @PostMapping("/vrfs/{vrfId}/sub-redes")
    public ResponseEntity<SubRedeResponse> criar(@PathVariable Long vrfId,
                                                 @Valid @RequestBody SubRedeCriacaoRequest req,
                                                 Authentication autenticacao,
                                                 UriComponentsBuilder uriBuilder) {
        SubRedeResponse criada = service.criar(vrfId, req.secaoId(), req.cidr(), 
        req.vlanId(), req.unidade(), req.descricao(), autenticacao.getName());

        URI local = uriBuilder.path("/api/sub-redes/{id}")
                .buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @GetMapping("/sub-redes/{id}")
    public SubRedeResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/sub-redes/{id}/filhas")
    public List<SubRedeResponse> listarFilhas(@PathVariable Long id) {
        return service.listarFilhas(id);
    }

    @GetMapping("/secoes/{secaoId}/sub-redes")
    public List<SubRedeResponse> listarPorSecao(@PathVariable Long secaoId) {
        return service.listarPorSecao(secaoId);
    }

    @PutMapping("/sub-redes/{id}")
    public SubRedeResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody SubRedeAtualizacaoRequest req) {
        return service.atualizar(id, req.secaoId(), req.vlanId(), req.unidade(), req.descricao());
    }

    @DeleteMapping("/sub-redes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/vlans/{vlanId}/sub-redes")
    public List<SubRedeResponse> listarPorVlan(@PathVariable Long vlanId) {
        return service.listarPorVlan(vlanId);
    }
}