-- Configuração visual da wiki (task 6.5).
--
-- É UMA linha, e o CHECK abaixo faz disso garantia do banco em vez de
-- convenção: sem ele, o dia em que uma segunda linha entrar ninguém saberá
-- qual vale, e o bug aparece como "a cor mudou sozinha".
--
-- ATENÇÃO ao ler esta tabela: o GET /settings é PÚBLICO, porque a tela de
-- login precisa do nome e do tema antes de existir sessão. Nada confidencial
-- pode entrar aqui — nem chave de integração, nem URL de webhook, nada.

CREATE TABLE settings (
    id            BIGINT       PRIMARY KEY,
    app_name      VARCHAR(60)  NOT NULL,
    logo_media_id UUID,
    theme         VARCHAR(30)  NOT NULL,
    primary_color VARCHAR(7),
    updated_at    TIMESTAMP(6),
    updated_by    BIGINT,

    CONSTRAINT ck_settings_linha_unica CHECK (id = 1),
    CONSTRAINT fk_settings_updated_by  FOREIGN KEY (updated_by)    REFERENCES users (id),

    -- Impede o logo apontar para mídia inexistente, e impede apagar a mídia
    -- que está sendo usada como logo
    CONSTRAINT fk_settings_logo        FOREIGN KEY (logo_media_id) REFERENCES media (id)
);

-- Linha padrão: sem ela o primeiro GET não teria o que devolver e o frontend
-- ficaria sem valores iniciais.
--
-- 'NEUTRAL' em MAIÚSCULAS: o @Enumerated(EnumType.STRING) grava e lê o NOME do
-- enum. Com 'neutral' minúsculo, o Theme.valueOf estoura na primeira leitura.
INSERT INTO settings (id, app_name, theme) VALUES (1, 'Wikify', 'NEUTRAL');
