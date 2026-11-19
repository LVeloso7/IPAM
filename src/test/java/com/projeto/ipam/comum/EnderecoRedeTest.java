package com.projeto.ipam.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnderecoRedeTest {

    @Test
    void aceitaCidrValidoERemoveEspacos() {
        assertEquals("10.0.5.0/24", EnderecoRede.normalizarCidr("  10.0.5.0/24 "));
    }

    @Test
    void normalizaIpv6ParaFormatoCanonico() {
        assertEquals("2001:db8::/32", EnderecoRede.normalizarCidr("2001:0db8:0000::/32"));
    }

    @Test
    void recusaCidrComBitsDeHostESugereOCorreto() {
        DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                () -> EnderecoRede.normalizarCidr("10.0.5.17/24"));
        assertTrue(erro.getMessage().contains("10.0.5.0/24"));
    }

    @Test
    void recusaRedeSemMascara() {
        assertThrows(DadoInvalidoException.class, () -> EnderecoRede.normalizarCidr("10.0.5.0"));
    }

    @Test
    void recusaZerosAEsquerda() {
        assertThrows(DadoInvalidoException.class, () -> EnderecoRede.normalizarCidr("010.0.5.0/24"));
    }

    @Test
    void recusaNotacaoAbreviada() {
        assertThrows(DadoInvalidoException.class, () -> EnderecoRede.normalizarIp("10.1"));
    }

    @Test
    void recusaTextoQueNaoEEndereco() {
        assertThrows(DadoInvalidoException.class, () -> EnderecoRede.normalizarCidr("rede-x"));
    }

    @Test
    void aceitaIpUnico() {
        assertEquals("10.0.5.17", EnderecoRede.normalizarIp(" 10.0.5.17 "));
    }

    @Test
    void recusaIpComMascara() {
        assertThrows(DadoInvalidoException.class, () -> EnderecoRede.normalizarIp("10.0.5.17/24"));
    }
}