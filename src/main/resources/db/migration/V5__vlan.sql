CREATE TABLE vlan (
    id        BIGSERIAL    PRIMARY KEY,
    secao_id  BIGINT       NOT NULL REFERENCES secao (id),
    numero    INTEGER      NOT NULL CHECK (numero BETWEEN 1 AND 4094),
    nome      VARCHAR(100) NOT NULL,
    descricao TEXT,
    CONSTRAINT uq_vlan_secao_numero UNIQUE (secao_id, numero),
    CONSTRAINT uq_vlan_id_secao UNIQUE (id, secao_id)
);

-- 1. Cria um registro de VLAN para cada par (seção, número) já usado.
INSERT INTO vlan (secao_id, numero, nome)
SELECT DISTINCT secao_id, vlan_id, 'VLAN ' || vlan_id
FROM sub_rede
WHERE vlan_id IS NOT NULL;

-- 2. Troca, em cada sub-rede, o número da VLAN pelo ID do registro.
UPDATE sub_rede s
SET vlan_id = v.id
FROM vlan v
WHERE v.secao_id = s.secao_id
  AND v.numero = s.vlan_id;

-- 3. A VLAN da sub-rede precisa ser da mesma seção que a sub-rede.
ALTER TABLE sub_rede
    ADD CONSTRAINT fk_subrede_vlan_secao
    FOREIGN KEY (vlan_id, secao_id) REFERENCES vlan (id, secao_id);

CREATE INDEX ix_subrede_vlan ON sub_rede (vlan_id);