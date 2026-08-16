package com.wikify.media;

import org.springframework.core.io.Resource;

import java.io.InputStream;


public interface MediaStorage {

    String store(InputStream content, String contentType);

    Resource open(String storageKey);

    void delete(String storageKey);

    String providerName();
}
