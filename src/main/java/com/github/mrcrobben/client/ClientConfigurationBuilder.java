package com.github.mrcrobben.client;

import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.ProxyProperties;

/**
 * A builder for creating {@link ClientConfiguration} instances.
 * <p>
 * This class provides methods to set AWS and proxy properties, allowing for a fluent API to build
 * a {@code ClientConfiguration} object with the desired settings.
 * </p>
 */
public class ClientConfigurationBuilder {

    /**
     * The AWS properties to be used in the client configuration.
     */
    private AwsProperties awsProperties;

    /**
     * The proxy properties to be used in the client configuration.
     */
    private ProxyProperties proxyProperties;

    /**
     * Sets the AWS properties for the client configuration.
     *
     * @param awsProperties the AWS properties to set. Must not be {@code null}.
     * @return this {@code ClientConfigurationBuilder} instance for method chaining.
     * @throws IllegalArgumentException if {@code awsProperties} is {@code null}.
     */
    public ClientConfigurationBuilder awsProperties(AwsProperties awsProperties) {
        if (awsProperties == null) {
            throw new IllegalArgumentException("AWS properties must not be null.");
        }
        this.awsProperties = awsProperties;
        return this;
    }

    /**
     * Sets the proxy properties for the client configuration.
     *
     * @param proxyProperties the proxy properties to set. Must not be {@code null}.
     * @return this {@code ClientConfigurationBuilder} instance for method chaining.
     * @throws IllegalArgumentException if {@code proxyProperties} is {@code null}.
     */
    public ClientConfigurationBuilder proxyProperties(ProxyProperties proxyProperties) {
        if (proxyProperties == null) {
            throw new IllegalArgumentException("Proxy properties must not be null.");
        }
        this.proxyProperties = proxyProperties;
        return this;
    }

    /**
     * Creates a new {@link ClientConfiguration} instance with the configured AWS and proxy properties.
     *
     * @return a new {@code ClientConfiguration} object initialized with the specified settings.
     */
    public ClientConfiguration createClientConfiguration() {
        return new ClientConfiguration(awsProperties, proxyProperties);
    }
}