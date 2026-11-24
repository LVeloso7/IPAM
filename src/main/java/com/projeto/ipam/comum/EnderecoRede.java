package com.projeto.ipam.comum;

import java.math.BigInteger;
import java.util.Locale;
import java.util.stream.Collectors;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddressStringParameters;
import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    // Endereços que não podem ser atribuídos: rede e broadcast (IPv4 até /30).
    public static List<String> enderecosReservados(String cidr) {
        IPAddress rede = paraEndereco(cidr);
        if (!rede.isIPv4() || rede.getNetworkPrefixLength() > 30) {
            return List.of();
        }
        return List.of(
                rede.getLower().withoutPrefixLength().toCanonicalString(),
                rede.getUpper().withoutPrefixLength().toCanonicalString());
    }

    // Primeiro endereço livre da rede, pulando reservados, usados e faixas de
    // filhas.
    public static Optional<String> proximoLivre(String cidr,
            Collection<String> cidrsFilhas,
            Collection<String> usados) {
        IPAddress rede = paraEndereco(cidr);
        List<IPAddress> filhas = cidrsFilhas.stream().map(EnderecoRede::paraEndereco).toList();
        Set<IPAddress> ocupados = usados.stream().map(EnderecoRede::paraEndereco)
                .collect(Collectors.toSet());
        Set<IPAddress> reservados = enderecosReservados(cidr).stream()
                .map(EnderecoRede::paraEndereco).collect(Collectors.toSet());

        IPAddress atual = rede.getLower().withoutPrefixLength();
        BigInteger ultimo = rede.getUpper().getValue();

        while (atual != null && atual.getValue().compareTo(ultimo) <= 0) {
            IPAddress candidato = atual;
            Optional<IPAddress> filha = filhas.stream()
                    .filter(f -> f.contains(candidato))
                    .findFirst();

            if (filha.isPresent()) {
                // Pula a faixa inteira da sub-rede filha de uma vez.
                atual = filha.get().getUpper().withoutPrefixLength().increment(1);
                continue;
            }
            if (!reservados.contains(atual) && !ocupados.contains(atual)) {
                return Optional.of(atual.toCanonicalString());
            }
            atual = atual.increment(1);
        }
        return Optional.empty();
    }

    // Valida um MAC de 48 bits e devolve no formato 08:00:2b:01:02:03.
    public static String normalizarMac(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        MACAddress mac = new MACAddressString(texto.strip()).getAddress();
        if (mac == null || mac.isMultiple() || mac.isExtended()) {
            throw new DadoInvalidoException("'" + texto.strip() + "' não é um endereço MAC válido");
        }
        return mac.toColonDelimitedString().toLowerCase(Locale.ROOT);
    }

    // Para valores que já vieram do banco, portanto válidos.
    private static IPAddress paraEndereco(String texto) {
        return new IPAddressString(texto).getAddress();
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