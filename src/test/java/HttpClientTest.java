import com.github.mrcrobben.client.ContentType;
import com.github.mrcrobben.client.HttpClient;
import com.github.mrcrobben.exception.ApiGatewayException;
import com.github.mrcrobben.model.AwsProperties;
import com.github.mrcrobben.model.HttpMethod;
import com.github.mrcrobben.model.ProxyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpClientTest {

    @Mock
    private SdkHttpClient sdkHttpClient;
    @Mock
    private ExecutableHttpRequest executableHttpRequest;
    @Mock
    private HttpExecuteResponse httpExecuteResponse;
    @Mock
    private SdkHttpResponse sdkHttpResponse;
    @Mock
    private AwsProperties awsProperties;
    @Mock
    private ProxyProperties proxyProperties;

    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        when(awsProperties.awsApiGatewayEndpoint()).thenReturn("https://example.com");
        when(awsProperties.serviceName()).thenReturn("execute-api");
        when(awsProperties.awsRegion()).thenReturn("us-west-2");
        when(awsProperties.awsIamAccessKey()).thenReturn("accessKey");
        when(awsProperties.awsSecretAccessKey()).thenReturn("secretKey");
        when(awsProperties.socketTimeout()).thenReturn(3000);

        httpClient = new HttpClient.Builder()
                .awsProperties(awsProperties)
                .proxyProperties(proxyProperties)
                .build();

        Field field = ReflectionUtils
                .findFields(HttpClient.class, f -> f.getName().equals("client"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        field.setAccessible(true);
        field.set(httpClient, sdkHttpClient);

    }

    @Test
    void testExecuteSuccessfulRequest() throws Exception {
        // Arrange
        String payload = "Test payload";
        ContentType contentType = ContentType.JSON;
        HttpMethod method = HttpMethod.POST;

        // Create an AbortableInputStream with a ByteArrayInputStream as the underlying stream
        InputStream underlyingStream = new ByteArrayInputStream("Response Body".getBytes());
        AbortableInputStream responseStream = AbortableInputStream.create(underlyingStream);

        when(httpExecuteResponse.responseBody()).thenReturn(Optional.of(responseStream));
        when(httpExecuteResponse.httpResponse()).thenReturn(sdkHttpResponse);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);

        when(sdkHttpClient.prepareRequest(any(HttpExecuteRequest.class))).thenReturn(executableHttpRequest);
        when(executableHttpRequest.call()).thenReturn(httpExecuteResponse);

        // Act
        InputStream resultStream = httpClient.execute(method, payload, contentType);

        // Assert
        assertEquals("Response Body", IoUtils.toUtf8String(resultStream));
        verify(sdkHttpClient, times(1)).prepareRequest(any(HttpExecuteRequest.class));
    }

    @Test
    void testExecuteRequestFailure() throws Exception {
        // Arrange
        String payload = "Test payload";
        ContentType contentType = ContentType.JSON;
        HttpMethod method = HttpMethod.POST;

        when(sdkHttpResponse.isSuccessful()).thenReturn(false);
        when(httpExecuteResponse.httpResponse()).thenReturn(sdkHttpResponse);

        when(sdkHttpClient.prepareRequest(any())).thenReturn(executableHttpRequest);
        when(executableHttpRequest.call()).thenReturn(httpExecuteResponse);
                // Act & Assert
        ApiGatewayException exception = assertThrows(ApiGatewayException.class, () -> {
            httpClient.execute(method, payload, contentType);
        });

        assertEquals("Status code: 0", exception.getMessage());
    }

    @Test
    void testExecuteEmptyResponseBody() throws Exception {
        // Arrange
        String payload = "Test payload";
        ContentType contentType = ContentType.JSON;
        HttpMethod method = HttpMethod.POST;

        when(httpExecuteResponse.httpResponse()).thenReturn(sdkHttpResponse);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);
        when(httpExecuteResponse.responseBody()).thenReturn(Optional.empty());

        when(sdkHttpClient.prepareRequest(any(HttpExecuteRequest.class))).thenReturn(executableHttpRequest);
        when(executableHttpRequest.call()).thenReturn(httpExecuteResponse);

        // Act & Assert
        ApiGatewayException exception = assertThrows(ApiGatewayException.class, () -> {
            httpClient.execute(method, payload, contentType);
        });

        assertEquals("Response body is empty!", exception.getMessage());
    }
}
