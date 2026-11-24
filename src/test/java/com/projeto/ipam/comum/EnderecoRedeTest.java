package com.projeto.ipam.comum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Optional;

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

    @Test
    void proximoLivrePulaOEnderecoDeRede() {
        assertEquals(Optional.of("10.0.5.1"),
                EnderecoRede.proximoLivre("10.0.5.0/24", List.of(), List.of()));
    }

    @Test
    void proximoLivrePulaFilhasEEnderecosUsados() {
        // A /26 cobre .0 a .63; .64 e .65 estão usados; o próximo livre é .66
        assertEquals(Optional.of("10.0.5.66"),
                EnderecoRede.proximoLivre("10.0.5.0/24",
                        List.of("10.0.5.0/26"),
                        List.of("10.0.5.64", "10.0.5.65")));
    }

    @Test
    void proximoLivreVazioQuandoARedeEstaCheia() {
        // Uma /30 tem só .1 e .2 utilizáveis
        assertEquals(Optional.empty(),
                EnderecoRede.proximoLivre("10.0.5.0/30", List.of(),
                        List.of("10.0.5.1", "10.0.5.2")));
    }

    @Test
    void reservadosDeUmaBarra24() {
        assertEquals(List.of("10.0.5.0", "10.0.5.255"),
                EnderecoRede.enderecosReservados("10.0.5.0/24"));
    }

    @Test
    void barra31NaoTemReservados() {
        assertEquals(List.of(), EnderecoRede.enderecosReservados("10.0.5.0/31"));
    }

    @Test
    void normalizaMacEmFormatosDiferentes() {
        assertEquals("08:00:2b:01:02:03", EnderecoRede.normalizarMac("08-00-2B-01-02-03"));
        assertEquals("08:00:2b:01:02:03", EnderecoRede.normalizarMac("0800.2b01.0203"));
    }

    @Test
    void recusaMacInvalido() {
        assertThrows(DadoInvalidoException.class, () -> EnderecoRede.normalizarMac("08:00:2b:zz"));
    }
}