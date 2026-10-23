-- Titulares da escalação (RN19). A formação fica em clubes.formacao.
CREATE TABLE escalacoes (
    clube_id    INTEGER NOT NULL REFERENCES clubes (id),
    jogador_id  INTEGER NOT NULL REFERENCES jogadores (id),
    criado_em   TIMESTAMP WITH TIME ZONE NOT NULL,

    PRIMARY KEY (clube_id, jogador_id),
    -- um jogador está em no máximo uma escalação
    CONSTRAINT uk_escalacoes_jogador UNIQUE (jogador_id)
);
