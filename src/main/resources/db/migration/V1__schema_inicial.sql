-- V1__schema_inicial.sql (Flyway)
CREATE TABLE vrf (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL UNIQUE,
    rd          VARCHAR(50),
    descricao   TEXT
);

CREATE TABLE sub_rede (
    id          BIGSERIAL PRIMARY KEY,
    vrf_id      BIGINT NOT NULL REFERENCES vrf(id),
    pai_id      BIGINT REFERENCES sub_rede(id),
    cidr        CIDR NOT NULL,
    vlan_id     BIGINT,
    unidade     VARCHAR(100),          -- ex.: Marabá, Maracanaú
    descricao   TEXT,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_por  VARCHAR(100) NOT NULL,
    CONSTRAINT uq_subrede_vrf UNIQUE (vrf_id, cidr)
);
CREATE INDEX ix_subrede_cidr ON sub_rede USING gist (cidr inet_ops);

CREATE TABLE endereco_ip (
    id           BIGSERIAL PRIMARY KEY,
    sub_rede_id  BIGINT NOT NULL REFERENCES sub_rede(id),
    endereco     INET NOT NULL,
    hostname     VARCHAR(255),
    mac          MACADDR,
    status       VARCHAR(20) NOT NULL,  -- ATIVO, RESERVADO, DHCP, LIVRE
    descricao    TEXT,
    CONSTRAINT uq_ip_subrede UNIQUE (sub_rede_id, endereco)
);
CREATE INDEX ix_ip_endereco ON endereco_ip USING gist (endereco inet_ops);