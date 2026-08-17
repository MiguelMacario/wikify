-- Migration Script: Criação da tabela de comentários

CREATE TABLE IF NOT EXISTS comment (
                                       id BIGSERIAL PRIMARY KEY,
                                       author_id BIGINT NOT NULL,
                                       document_id BIGINT NOT NULL,
                                       content TEXT NOT NULL,
                                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
                                       CONSTRAINT fk_comment_author
                                       FOREIGN KEY (author_id)
    REFERENCES users (id)
    ON DELETE CASCADE,

    CONSTRAINT fk_comment_document
    FOREIGN KEY (document_id)
    REFERENCES documents (id)
    ON DELETE CASCADE
    );

-- Índices de otimização
CREATE INDEX IF NOT EXISTS idx_comment_document_id ON comment (document_id);
CREATE INDEX IF NOT EXISTS idx_comment_author_id ON comment (author_id);