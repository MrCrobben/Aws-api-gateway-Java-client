package com.github.mrcrobben.model;

import software.amazon.awssdk.http.SdkHttpMethod;

public enum HttpMethod {

    POST(SdkHttpMethod.POST),
    GET(SdkHttpMethod.GET),
    PATCH(SdkHttpMethod.PATCH);

    private HttpMethod(final SdkHttpMethod sdkHttpMethod) {
    }

    public SdkHttpMethod getMethod() {
        return SdkHttpMethod.valueOf(this.name());
    }
}
