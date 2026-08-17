-- Conteúdo da página inicial da wiki (/docs sem documento selecionado).
--
-- Vai em `settings` e não numa tabela nova porque é conteúdo global, único e
-- editado pelo SYSADMIN — exatamente o mesmo escopo de nome, tema e logo.
--
-- Migration separada da V5 de propósito: a V5 pode já ter sido aplicada, e
-- editar migration aplicada quebra o checksum do Flyway.

ALTER TABLE settings ADD COLUMN home_title   VARCHAR(120);
ALTER TABLE settings ADD COLUMN home_content TEXT;

-- Nulo em home_title é intencional: quando não preenchido, a tela usa o
-- app_name. Assim renomear a wiki renomeia a home junto, sem editar dois campos.
UPDATE settings
SET home_content = 'Escolha um documento na barra lateral para começar.'
WHERE id = 1;
