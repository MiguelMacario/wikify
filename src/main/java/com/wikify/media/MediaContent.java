package com.wikify.media;

import org.springframework.core.io.Resource;

public record MediaContent(Resource resource, String contentType, long sizeBytes) {
}
