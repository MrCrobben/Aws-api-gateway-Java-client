package com.github.mrcrobben.client;

/**
 * Factory class for creating {@link HttpClient} instances with the specified client configuration.
 * <p>
 * This class provides a static method to create and configure a new {@code HttpClient} instance
 * using the provided {@link ClientConfiguration}.
 * </p>
 */
public class ClientFactory {

    /**
     * Creates a new {@link HttpClient} instance using the provided client configuration.
     *
     * @param clientConfiguration the client configuration containing AWS and proxy properties. Must not be {@code null}.
     * @return a new {@code HttpClient} instance configured with the settings from the given {@code ClientConfiguration}.
     * @throws IllegalArgumentException if {@code clientConfiguration} is {@code null}.
     */
    public static HttpClient create(final ClientConfiguration clientConfiguration) {
        if (clientConfiguration == null) {
            throw new IllegalArgumentException("Client configuration must not be null.");
        }

        return new HttpClient.Builder()
                .awsProperties(clientConfiguration.getAwsProperties())
                .proxyProperties(clientConfiguration.getProxyProperties())
                .build();
    }
}
