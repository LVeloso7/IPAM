package com.projeto.ipam.busca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/busca")
public class BuscaController {

    private final BuscaService service;

    public BuscaController(BuscaService service) {
        this.service = service;
    }

    @GetMapping
    public ResultadoBusca buscar(@RequestParam("q") String termo) {
        return service.buscar(termo);
    }
}