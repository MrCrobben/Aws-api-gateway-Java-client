package com.github.mrcrobben.client;

import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.ProxyProperties;

public class ClientConfiguration {

    private AwsProperties awsProperties;
    private ProxyProperties proxyProperties;

    ClientConfiguration(final AwsProperties awsProperties, final ProxyProperties proxyProperties) {
        this.awsProperties = awsProperties;
        this.proxyProperties = proxyProperties;
    }

}
