package com.wikify.entity.enums;

/**
 * Quem pode editar um documento, decidido pelo gestor documento a documento.
 * O gestor do departamento edita qualquer um dos dois casos.
 */
public enum EditPolicy {
    /** Qualquer contribuidor do departamento — é o que torna a wiki colaborativa. */
    DEPARTMENT,
    /** Só quem criou o documento. Para conteúdo que precisa de controle. */
    AUTHOR_ONLY
}
