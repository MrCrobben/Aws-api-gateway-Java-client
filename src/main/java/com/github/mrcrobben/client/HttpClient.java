package com.github.mrcrobben.client;

import com.github.mrcrobben.exception.ApiGatewayException;
import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.HttpMethod;
import com.github.mrcrobben.model.ProxyProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.logging.Logger;

public class HttpClient {

    private static final Logger logger = Logger.getLogger(HttpClient.class.getName());

    private SdkHttpClient client;
    private AwsProperties awsProperties;
    private ProxyProperties proxyProperties;

    // Private constructor to enforce usage of the Builder
    private HttpClient(Builder builder) {
        this.awsProperties = builder.awsProperties;
        this.proxyProperties = builder.proxyProperties;

        client = getSdkHttpClient();
    }

    // Example method to execute an HTTP request
    public InputStream execute(final HttpMethod method, final String payload, final ContentType contentType) {

        final var preparedRequest = getPreparedRequest(method, payload, contentType);
        final var signedRequest = getSignedRequest(preparedRequest);

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

            try(final var responseStream = response.responseBody()
                    .orElseThrow(() -> new ApiGatewayException("Response body is empty!"))){

                return responseStream.delegate();
            }


        } catch (IOException e) {
            throw new ApiGatewayException(e);
        }
    }
    private SdkHttpFullRequest getPreparedRequest(final HttpMethod method, final String payload,
                                                  final ContentType contentType) {

        final var contentStream = RequestBody.fromString(payload)
                .contentStreamProvider();

        return SdkHttpFullRequest.builder()
                .method(method.getMethod())
                .uri(URI.create(awsProperties.awsApiGatewayEndpoint()))
                .putHeader("Content-Type", contentType.getContentType())
                .contentStreamProvider(contentStream)
                .build();
    }
    private SignedRequest getSignedRequest(final SdkHttpFullRequest request) {

        final var signer = AwsV4HttpSigner.create();
        final var credentials = getCredentials();

        return signer.sign(r -> r.identity(credentials)
                .request(request)
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, awsProperties.serviceName())
                .putProperty(AwsV4HttpSigner.REGION_NAME, awsProperties.awsRegion()));
    }

    private AwsCredentialsIdentity getCredentials() {

        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsProperties.awsIamAccessKey(), awsProperties.awsSecretAccessKey()))
                .resolveCredentials();

    }

    private SdkHttpClient getSdkHttpClient() {

        final var proxyConfiguration = getProxyConfiguration();

        return ApacheHttpClient.builder()
                .proxyConfiguration(proxyConfiguration)
                .socketTimeout(Duration.ofMillis(Integer.toUnsignedLong(awsProperties.socketTimeout())))
                .build();
    }

    private ProxyConfiguration getProxyConfiguration() {

        final var proxyConfig = ProxyConfiguration.builder();

        if (proxyProperties == null || !proxyProperties.enabled()) {

            proxyConfig.useEnvironmentVariableValues(false);
            proxyConfig.useSystemPropertyValues(false);

        } else {

            proxyConfig.username(proxyProperties.username());
            proxyConfig.password(proxyProperties.password());
            proxyConfig.endpoint(getEndpoint());
        }

        return proxyConfig.build();
    }

    private URI getEndpoint() {
        return URI.create(proxyProperties.host() + ":" + proxyProperties.port());
    }

    // Static nested Builder class
    public static class Builder {
        private AwsProperties awsProperties;
        private ProxyProperties proxyProperties;

        public Builder awsProperties(final AwsProperties awsProperties) {
            this.awsProperties = awsProperties;
            return this;
        }

        public Builder proxyProperties(final ProxyProperties proxyProperties) {
            this.proxyProperties = proxyProperties;
            return this;
        }

        public HttpClient build() {
            return new HttpClient(this);
        }
    }

    private String handleHttpErrorResponse(final SdkHttpResponse response) {
        final var sb = new StringBuilder();
        sb.append("Status code: ").append(response.statusCode());
        response.statusText().ifPresent(txt -> sb.append("Status text: ").append(txt));
        return sb.toString();
    }
}
