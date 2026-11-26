package com.projeto.ipam.vlan;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class VlanController {

    private final VlanService service;

    public VlanController(VlanService service) {
        this.service = service;
    }

    @GetMapping("/secoes/{secaoId}/vlans")
    public List<VlanResponse> listarPorSecao(@PathVariable Long secaoId) {
        return service.listarPorSecao(secaoId).stream().map(VlanResponse::de).toList();
    }

    @PostMapping("/secoes/{secaoId}/vlans")
    public ResponseEntity<VlanResponse> criar(@PathVariable Long secaoId,
                                              @Valid @RequestBody VlanRequest req,
                                              UriComponentsBuilder uriBuilder) {
        Vlan vlan = service.criar(secaoId, req.numero(), req.nome(), req.descricao());
        URI local = uriBuilder.path("/api/vlans/{id}").buildAndExpand(vlan.getId()).toUri();
        return ResponseEntity.created(local).body(VlanResponse.de(vlan));
    }

    @GetMapping("/vlans/{id}")
    public VlanResponse buscar(@PathVariable Long id) {
        return VlanResponse.de(service.buscarPorId(id));
    }

    @PutMapping("/vlans/{id}")
    public VlanResponse atualizar(@PathVariable Long id, @Valid @RequestBody VlanRequest req) {
        return VlanResponse.de(service.atualizar(id, req.numero(), req.nome(), req.descricao()));
    }

    @DeleteMapping("/vlans/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}