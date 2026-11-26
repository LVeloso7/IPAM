CREATE TABLE secao (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    descricao TEXT
);
CREATE UNIQUE INDEX uq_secao_nome_lower ON secao (LOWER(nome));

-- Seção padrão para as sub-redes que já existem.
INSERT INTO secao (nome, descricao)
VALUES ('Geral', 'Sub-redes cadastradas antes da criação das seções');

-- Passo 1: coluna nova, ainda opcional.
ALTER TABLE sub_rede ADD COLUMN secao_id BIGINT REFERENCES secao (id);

-- Passo 2: preenche os registros existentes.
UPDATE sub_rede SET secao_id = (SELECT id FROM secao WHERE nome = 'Geral');

-- Passo 3: agora pode ser obrigatória.
ALTER TABLE sub_rede ALTER COLUMN secao_id SET NOT NULL;

CREATE INDEX ix_subrede_secao ON sub_rede (secao_id);