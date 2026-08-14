-- Busca full-text (task 2.8).
--
-- Coluna gerada pelo próprio Postgres: ela é recalculada sozinha a cada INSERT
-- ou UPDATE em title/content_markdown. Nenhum código Java precisa mantê-la.
--
-- O título entra com peso 'A' e o conteúdo com 'B', então o ts_rank coloca um
-- documento cujo TÍTULO casa com o termo acima de outro que só menciona o termo
-- no meio do texto.
--
-- Detalhe que costuma travar quem escreve isso pela primeira vez: coluna gerada
-- exige expressão IMMUTABLE. O to_tsvector de DOIS argumentos (com a
-- configuração de idioma explícita, como 'portuguese' abaixo) é immutable; o de
-- um argumento só depende da configuração da sessão, é apenas STABLE, e o
-- Postgres recusa com "generation expression is not immutable".

ALTER TABLE documents ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('portuguese', coalesce(title, '')),            'A') ||
        setweight(to_tsvector('portuguese', coalesce(content_markdown, '')), 'B')
    ) STORED;

-- GIN é o índice certo para tsvector: ele indexa cada lexema separadamente,
-- que é o que permite responder "quais documentos contêm esta palavra" sem
-- varrer a tabela.
CREATE INDEX idx_documents_search ON documents USING GIN (search_vector);
