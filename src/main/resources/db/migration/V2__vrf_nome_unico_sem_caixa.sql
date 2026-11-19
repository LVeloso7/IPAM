/*Garante que um nome seja único, independente de maiusculas ou minúsculas */
CREATE UNIQUE INDEX uq_vrf_nome_lower ON vrf (LOWER(nome));