package com.github.mrcrobben.client;

import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.ProxyProperties;

public class ClientConfigurationBuilder {
    private AwsProperties awsProperties;
    private ProxyProperties proxyProperties;

    public ClientConfigurationBuilder awsProperties(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        return this;
    }

    public ClientConfigurationBuilder proxyProperties(ProxyProperties proxyProperties) {
        this.proxyProperties = proxyProperties;
        return this;
    }

    public ClientConfiguration createClientConfiguration() {
        return new ClientConfiguration(awsProperties, proxyProperties);
    }
}