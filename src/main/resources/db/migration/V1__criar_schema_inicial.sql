CREATE TABLE jogador (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    nome_personagem VARCHAR(120) NOT NULL
);

CREATE UNIQUE INDEX uk_jogador_nome ON jogador (LOWER(nome));

CREATE TABLE campanha (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    sistema_rpg VARCHAR(120) NOT NULL
);

CREATE UNIQUE INDEX uk_campanha_nome ON campanha (LOWER(nome));

CREATE TABLE sessao (
    id UUID PRIMARY KEY,
    campanha_id UUID NOT NULL REFERENCES campanha(id) ON DELETE RESTRICT,
    titulo VARCHAR(160) NOT NULL,
    data_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    capacidade_maxima INTEGER NOT NULL CHECK (capacidade_maxima > 0),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('AO_VIVO', 'POR_TEXTO')),
    plataforma VARCHAR(120) NOT NULL,
    local_acesso VARCHAR(500) NOT NULL,
    regra_participacao VARCHAR(20) NOT NULL CHECK (regra_participacao IN ('LIVRE', 'CONVITE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('AGENDADA', 'EM_ANDAMENTO', 'FINALIZADA', 'CANCELADA'))
);

CREATE INDEX idx_sessao_campanha ON sessao(campanha_id);
CREATE INDEX idx_sessao_data_hora ON sessao(data_hora);

CREATE TABLE sessao_convidado (
    sessao_id UUID NOT NULL REFERENCES sessao(id) ON DELETE CASCADE,
    jogador_id UUID NOT NULL REFERENCES jogador(id) ON DELETE RESTRICT,
    PRIMARY KEY (sessao_id, jogador_id)
);

CREATE TABLE sessao_participante (
    sessao_id UUID NOT NULL REFERENCES sessao(id) ON DELETE CASCADE,
    jogador_id UUID NOT NULL REFERENCES jogador(id) ON DELETE RESTRICT,
    presenca VARCHAR(20) NOT NULL CHECK (presenca IN ('PENDENTE', 'PRESENTE', 'AUSENTE')),
    PRIMARY KEY (sessao_id, jogador_id)
);
