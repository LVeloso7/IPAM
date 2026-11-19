package com.projeto.ipam.vrf;

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
@RequestMapping("/api/vrfs")
public class VrfController {

    private final VrfService service;

    public VrfController(VrfService service) {
        this.service = service;
    }

    @GetMapping
    public List<VrfResponse> listar() {
        return service.listar().stream()
                .map(VrfResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public VrfResponse buscar(@PathVariable Long id) {
        return VrfResponse.de(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VrfResponse> criar(@Valid @RequestBody VrfRequest req,
                                             UriComponentsBuilder uriBuilder) {
        Vrf vrf = service.criar(req.nome(), req.rd(), req.descricao());
        URI local = uriBuilder.path("/api/vrfs/{id}").buildAndExpand(vrf.getId()).toUri();
        return ResponseEntity.created(local).body(VrfResponse.de(vrf));
    }

    @PutMapping("/{id}")
    public VrfResponse atualizar(@PathVariable Long id, @Valid @RequestBody VrfRequest req) {
        return VrfResponse.de(service.atualizar(id, req.nome(), req.rd(), req.descricao()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}