/* Endereço IP: Integridade e Auditoria*/

-- 1. Índice que faltou na V1: as consultas de filhas filtram por pai_id.
CREATE INDEX ix_subrede_pai ON sub_rede (pai_id);

-- 2. Permite que outras tabelas referenciem o par (sub-rede, VRF).
ALTER TABLE sub_rede ADD CONSTRAINT uq_subrede_id_vrf UNIQUE (id, vrf_id);

-- 3. Novas colunas: a VRF do IP e os campos de auditoria.
ALTER TABLE endereco_ip
    ADD COLUMN vrf_id         BIGINT       NOT NULL REFERENCES vrf (id),
    ADD COLUMN criado_em      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    ADD COLUMN criado_por     VARCHAR(100) NOT NULL,
    ADD COLUMN atualizado_em  TIMESTAMPTZ,
    ADD COLUMN atualizado_por VARCHAR(100);

-- 4. Garante que a VRF do IP é a mesma VRF da sua sub-rede.
ALTER TABLE endereco_ip
    ADD CONSTRAINT fk_ip_subrede_vrf
    FOREIGN KEY (sub_rede_id, vrf_id) REFERENCES sub_rede (id, vrf_id);

-- 5. Unicidade por VRF, e não mais por sub-rede.
ALTER TABLE endereco_ip DROP CONSTRAINT uq_ip_subrede;
ALTER TABLE endereco_ip ADD CONSTRAINT uq_ip_vrf UNIQUE (vrf_id, endereco);

-- 6. Índice para listar os IPs de uma sub-rede.
CREATE INDEX ix_ip_subrede ON endereco_ip (sub_rede_id);

-- 7. Restrições de domínio.
ALTER TABLE endereco_ip ADD CONSTRAINT ck_ip_status
    CHECK (status IN ('EM_USO', 'RESERVADO', 'DHCP'));

ALTER TABLE endereco_ip ADD CONSTRAINT ck_ip_sem_mascara
    CHECK (masklen(endereco) = CASE family(endereco) WHEN 4 THEN 32 ELSE 128 END);