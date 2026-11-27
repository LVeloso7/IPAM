package com.projeto.ipam.painel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {

    private final PainelService service;

    public PainelController(PainelService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo() {
        return service.resumo();
    }
}