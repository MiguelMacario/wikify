-- Tabela de mídia (task 6.2).
--
-- Guarda o REGISTRO do arquivo, não o arquivo: os bytes ficam em disco, sob o
-- caminho de `wikify.media.root`, e `storage_key` é a chave opaca que a
-- implementação de MediaStorage sabe traduzir.
--
-- Consequência operacional que vale repetir: com a mídia fora do banco, um
-- pg_dump não leva mais tudo. Backup do banco sem backup do diretório restaura
-- COM SUCESSO uma wiki com todas as imagens quebradas — e isso só aparece
-- quando alguém abre um documento.

CREATE TABLE media (
    -- UUID, e não BIGINT como as outras tabelas, porque este id vai dentro da
    -- URL: com sequencial dá para varrer /media/1, /media/2 e descobrir quantos
    -- arquivos existem, mesmo sem conseguir abrir nenhum.
    id                UUID          PRIMARY KEY,

    storage_key       VARCHAR(255)  NOT NULL,

    -- Por linha, não configuração global: é o que permite migrar de storage
    -- arquivo a arquivo, com os dois destinos convivendo.
    provider          VARCHAR(50)   NOT NULL,

    -- MIME real, apurado pelos bytes iniciais no upload
    content_type      VARCHAR(100)  NOT NULL,
    size_bytes        BIGINT        NOT NULL,

    -- Só para exibir e depurar; nunca vira nome de arquivo em disco
    original_filename VARCHAR(255),

    department_id     BIGINT        NOT NULL,
    uploaded_by       BIGINT        NOT NULL,
    created_at        TIMESTAMP(6)  NOT NULL,

    CONSTRAINT fk_media_department  FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_media_uploaded_by FOREIGN KEY (uploaded_by)   REFERENCES users (id)
);

-- O PostgreSQL não indexa chave estrangeira sozinho, e esta é a coluna que
-- toda leitura de mídia consulta para decidir permissão.
CREATE INDEX idx_media_department ON media (department_id);
