package com.github.mrcrobben.client;

import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.ProxyProperties;

public class ClientFactory {


    public static HttpClient create(final AwsProperties awsProperties, final ProxyProperties proxyProperties) {
        return new HttpClient.Builder()
                .awsProperties(awsProperties)
                .proxyProperties(proxyProperties)
                .build();
    }
}
