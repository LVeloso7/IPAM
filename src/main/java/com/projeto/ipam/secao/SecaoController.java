package com.projeto.ipam.secao;

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
@RequestMapping("/api/secoes")
public class SecaoController {

    private final SecaoService service;

    public SecaoController(SecaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<SecaoResponse> listar() {
        return service.listar().stream().map(SecaoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public SecaoResponse buscar(@PathVariable Long id) {
        return SecaoResponse.de(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<SecaoResponse> criar(@Valid @RequestBody SecaoRequest req,
                                               UriComponentsBuilder uriBuilder) {
        Secao secao = service.criar(req.nome(), req.descricao());
        URI local = uriBuilder.path("/api/secoes/{id}").buildAndExpand(secao.getId()).toUri();
        return ResponseEntity.created(local).body(SecaoResponse.de(secao));
    }

    @PutMapping("/{id}")
    public SecaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody SecaoRequest req) {
        return SecaoResponse.de(service.atualizar(id, req.nome(), req.descricao()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}