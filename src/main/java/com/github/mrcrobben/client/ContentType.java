package com.github.mrcrobben.client;

public enum ContentType {

    JSON("application/json");

    ContentType(String contentType) {
    }

    public String getContentType() {
        return this.name();
    }
}
