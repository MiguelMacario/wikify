package com.wikify.dto;

import com.wikify.entity.Document;

import java.util.ArrayList;
import java.util.List;

public record DocumentTreeNode(Long id, Long departmentId, String title, String url,
                               List<DocumentTreeNode> children) {

    public static DocumentTreeNode from(Document document) {
        return new DocumentTreeNode(
                document.getId(),
                document.getDepartment().getId(),
                document.getTitle(),
                "/docs/" + document.getPath(),
                new ArrayList<>());
    }
}
