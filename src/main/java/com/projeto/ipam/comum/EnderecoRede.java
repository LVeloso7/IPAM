package com.projeto.ipam.comum;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringParameters;

public final class EnderecoRede {

    private static final IPAddressStringParameters FORMATO_ESTRITO;

    static {
        IPAddressStringParameters.Builder construtor = new IPAddressStringParameters.Builder();
        construtor.allowEmpty(false);
        construtor.allowSingleSegment(false);
        construtor.getIPv4AddressParametersBuilder().allow_inet_aton(false);
        construtor.getIPv4AddressParametersBuilder().allowLeadingZeros(false);
        FORMATO_ESTRITO = construtor.toParams();
    }

    private EnderecoRede() {
    }

    // Valida um CIDR e devolve no formato canônico (ex.: "10.0.5.0/24").
    public static String normalizarCidr(String texto) {
        IPAddress endereco = interpretar(texto);

        if (!endereco.isPrefixed()) {
            throw new DadoInvalidoException(
                    "Informe a rede com a máscara, no formato 10.0.5.0/24");
        }
        if (!endereco.isPrefixBlock()) {
            String sugestao = endereco.toPrefixBlock().toCanonicalString();
            throw new DadoInvalidoException("'" + texto.strip()
                    + "' não é um endereço de rede válido. Você quis dizer " + sugestao + "?");
        }
        return endereco.toCanonicalString();
    }

    // Valida um IP único e devolve no formato canônico (ex.: "10.0.5.17").
    public static String normalizarIp(String texto) {
        IPAddress endereco = interpretar(texto);

        if (endereco.isPrefixed() || endereco.isMultiple()) {
            throw new DadoInvalidoException("Informe um único endereço IP, sem máscara");
        }
        return endereco.toCanonicalString();
    }

    private static IPAddress interpretar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DadoInvalidoException("O endereço é obrigatório");
        }
        IPAddress endereco = new IPAddressString(texto.strip(), FORMATO_ESTRITO).getAddress();
        if (endereco == null) {
            throw new DadoInvalidoException("'" + texto.strip() + "' não é um endereço IP válido");
        }
        return endereco;
    }
}