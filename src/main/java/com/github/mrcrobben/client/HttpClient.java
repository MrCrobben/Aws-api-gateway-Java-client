package com.github.mrcrobben.client;

import com.github.mrcrobben.exception.ApiGatewayException;
import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.HttpMethod;
import com.github.mrcrobben.model.ProxyProperties;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * A client for executing HTTP requests to an AWS API Gateway using AWS SDK's HTTP client.
 * <p>
 * This class provides methods for creating and sending HTTP requests with AWS signing and proxy configuration.
 * It supports a fluent API for building the client using the nested {@link Builder} class.
 * </p>
 */
public class HttpClient {

    private static final Logger logger = Logger.getLogger(HttpClient.class.getName());

    private final SdkHttpClient client;
    private final AwsProperties awsProperties;
    private final ProxyProperties proxyProperties;

    /**
     * Private constructor to enforce usage of the {@link Builder}.
     *
     * @param builder the {@link Builder} instance used to construct this {@code HttpClient}.
     */
    private HttpClient(Builder builder) {
        this.awsProperties = builder.awsProperties;
        this.proxyProperties = builder.proxyProperties;
        this.client = getSdkHttpClient();
    }

    /**
     * Executes an HTTP request with the specified method, payload, and content type.
     *
     * @param method the HTTP method to use for the request (e.g., GET, POST). Must not be {@code null}.
     * @param payloadString the payload to send in the request body. Must not be {@code null}.
     * @param contentType the content type of the payload. Must not be {@code null}.
     * @return an {@link InputStream} containing the response body.
     * @throws ApiGatewayException if the request fails or the response body is empty.
     */
    public InputStream execute(final HttpMethod method, final String payloadString, final ContentType contentType) {
        final var payload = ContentStreamProvider.fromUtf8String(payloadString);
        final var preparedRequest = getPreparedRequest(method, payload, contentType);
        final var signedRequest = getSignedRequest(preparedRequest, payload);

        final var httpExecuteRequest = HttpExecuteRequest.builder()
                .request(signedRequest.request())
                .contentStreamProvider(signedRequest.payload().orElse(null))
                .build();

        try {
            final var response = client.prepareRequest(httpExecuteRequest).call();

            if (!response.httpResponse().isSuccessful()) {
                final var handleError = handleHttpErrorResponse(response.httpResponse());

                logger.warning(String.format("Request failed!\nReason: %s", handleError));

                throw new ApiGatewayException(handleError);
            }

            return response.responseBody()
                    .map(StreamSource::new)
                    .orElseThrow(() -> new ApiGatewayException("Response body is empty!"))
                    .getInputStream();

        } catch (IOException e) {
            throw new ApiGatewayException(e);
        }
    }

    /**
     * Prepares an HTTP request based on the specified method, payload, and content type.
     *
     * @param method the HTTP method for the request. Must not be {@code null}.
     * @param payload the payload for the request body. Must not be {@code null}.
     * @param contentType the content type of the payload. Must not be {@code null}.
     * @return a {@link SdkHttpFullRequest} object representing the prepared request.
     */
    private SdkHttpFullRequest getPreparedRequest(final HttpMethod method, final ContentStreamProvider payload,
                                                  final ContentType contentType) {

        return SdkHttpFullRequest.builder()
                .method(method.getMethod())
                .uri(URI.create(awsProperties.awsApiGatewayEndpoint()))
                .putHeader("Content-Type", contentType.getContentType())
                .contentStreamProvider(payload)
                .build();
    }

    /**
     * Signs the prepared request using AWS V4 signing.
     *
     * @param request the request to sign. Must not be {@code null}.
     * @return a {@link SignedRequest} object representing the signed request.
     */
    private SignedRequest getSignedRequest(final SdkHttpFullRequest request,
                                           final ContentStreamProvider payload) {
        final var signer = AwsV4HttpSigner.create();
        final var credentials = getCredentials();

        return signer.sign(r -> r.identity(credentials)
                .request(request)
                .payload(payload)
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, awsProperties.serviceName())
                .putProperty(AwsV4HttpSigner.REGION_NAME, awsProperties.awsRegion()));
    }

    /**
     * Retrieves AWS credentials based on the properties configured in this client.
     *
     * @return an {@link AwsCredentialsIdentity} object containing the AWS credentials.
     */
    private AwsCredentialsIdentity getCredentials() {
        return AwsCredentialsIdentity.create(awsProperties.awsIamAccessKey(), awsProperties.awsSecretAccessKey());
    }

    /**
     * Creates an instance of {@link SdkHttpClient} with proxy and timeout settings.
     *
     * @return an {@link SdkHttpClient} instance.
     */
    private SdkHttpClient getSdkHttpClient() {
        final var proxyConfiguration = getProxyConfiguration();

        return ApacheHttpClient.builder()
                .proxyConfiguration(proxyConfiguration)
                .socketTimeout(Duration.ofMillis(Integer.toUnsignedLong(awsProperties.socketTimeout())))
                .build();
    }

    /**
     * Configures proxy settings based on the provided proxy properties.
     *
     * @return a {@link ProxyConfiguration} object.
     */
    private ProxyConfiguration getProxyConfiguration() {
        final var proxyConfig = ProxyConfiguration.builder()
                .useEnvironmentVariableValues(false)
                .useSystemPropertyValues(false);

        if (proxyProperties != null && !proxyProperties.enabled()) {

            proxyConfig.username(proxyProperties.username());
            proxyConfig.password(proxyProperties.password());
            proxyConfig.endpoint(getEndpoint());
        }

        return proxyConfig.build();
    }

    /**
     * Constructs the URI for the proxy endpoint.
     *
     * @return a {@link URI} representing the proxy endpoint.
     */
    private URI getEndpoint() {
        return URI.create(proxyProperties.host() + ":" + proxyProperties.port());
    }

    /**
     * Handles HTTP error responses by formatting error details.
     *
     * @param response the HTTP response to handle. Must not be {@code null}.
     * @return a string representing the formatted error details.
     */
    private String handleHttpErrorResponse(final SdkHttpResponse response) {
        final var sb = new StringBuilder();
        sb.append("Status code: ").append(response.statusCode());
        response.statusText().ifPresent(txt -> sb.append("Status text: ").append(txt));
        return sb.toString();
    }

    /**
     * Builder class for creating {@link HttpClient} instances.
     */
    public static class Builder {
        private AwsProperties awsProperties;
        private ProxyProperties proxyProperties;

        /**
         * Sets the AWS properties for the {@code HttpClient}.
         *
         * @param awsProperties the AWS properties to set. Must not be {@code null}.
         * @return this {@code Builder} instance for method chaining.
         * @throws IllegalArgumentException if {@code awsProperties} is {@code null}.
         */
        public Builder awsProperties(final AwsProperties awsProperties) {
            if (awsProperties == null) {
                throw new IllegalArgumentException("AWS properties must not be null.");
            }
            this.awsProperties = awsProperties;
            return this;
        }

        /**
         * Sets the proxy properties for the {@code HttpClient}.
         *
         * @param proxyProperties the proxy properties to set. May be {@code null}.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder proxyProperties(final ProxyProperties proxyProperties) {
            this.proxyProperties = proxyProperties;
            return this;
        }

        /**
         * Builds and returns a new {@link HttpClient} instance with the configured properties.
         *
         * @return a new {@code HttpClient} instance.
         */
        public HttpClient build() {
            return new HttpClient(this);
        }
    }
}
