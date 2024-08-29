package com.github.mrcrobben.client;

import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.ProxyProperties;

/**
 * Represents the configuration settings for a client, including AWS and proxy properties.
 * <p>
 * This class is used to encapsulate and manage the configuration required for initializing
 * a client with specific AWS properties and proxy settings.
 * </p>
 */
public class ClientConfiguration {

    /**
     * The AWS properties for the client configuration.
     */
    private final AwsProperties awsProperties;

    /**
     * The proxy properties for the client configuration.
     */
    private final ProxyProperties proxyProperties;

    /**
     * Constructs a {@code ClientConfiguration} with the specified AWS and proxy properties.
     *
     * @param awsProperties the AWS properties to be used for client configuration. Must not be {@code null}.
     * @param proxyProperties the proxy properties to be used for client configuration. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code awsProperties} or {@code proxyProperties} is {@code null}.
     */
    ClientConfiguration(final AwsProperties awsProperties, final ProxyProperties proxyProperties) {
        if (awsProperties == null) {
            throw new IllegalArgumentException("AWS properties must not be null.");
        }
        if (proxyProperties == null) {
            throw new IllegalArgumentException("Proxy properties must not be null.");
        }
        this.awsProperties = awsProperties;
        this.proxyProperties = proxyProperties;
    }

    public AwsProperties getAwsProperties() {
        return awsProperties;
    }

    public ProxyProperties getProxyProperties() {
        return proxyProperties;
    }
}
