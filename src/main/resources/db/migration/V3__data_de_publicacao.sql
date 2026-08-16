-- Data de publicação do documento.
--
-- Não dava para reaproveitar `updated_at`: ele muda a cada edição, então um
-- documento publicado em janeiro e corrigido hoje apareceria como publicado
-- hoje. `created_at` também não serve — é quando o rascunho nasceu, que pode
-- ser bem antes de alguém apertar "Publicar".
--
-- Fica nulo enquanto o documento é rascunho e é preenchido a cada publicação.
ALTER TABLE documents ADD COLUMN published_at TIMESTAMP(6);

-- Preenche o que já está publicado. Estes documentos foram publicados antes de
-- a coluna existir, então a data real se perdeu: `updated_at` é a melhor
-- aproximação disponível, e não a data exata.
UPDATE documents
SET published_at = COALESCE(updated_at, created_at)
WHERE status = 'PUBLISHED'
  AND published_at IS NULL;
