# AWS API Gateway Java Client

This library provides a simple and efficient Java client for interacting with AWS API Gateway. It utilizes AWS SDK's HTTP client to execute HTTP requests with AWS V4 signing and optional proxy configuration. The library is designed to be flexible, allowing for easy integration with different AWS services.

Currently client is using Apache client for issuing requests. There is currently no support for other AWS services. Only processing API calls on AWS machines protected by IAM security.

## Features

- **AWS V4 Signing**: Automatically sign requests using AWS V4 signing.
- **Proxy Support**: Configure proxy settings for requests if needed.
- **Fluent API**: Build and configure the client using a fluent API.
- **Error Handling**: Provides custom exceptions for handling API Gateway errors.

## Getting Started
### 1. Create AWS and Proxy Configuration

First, you need to set up your AWS and proxy configuration properties. 
```java
    AwsProperties awsProperties = new AwsProperties(
        "your-access-key",
        "your-secret-key",
        "us-west-2",
        "https://your-api-gateway-endpoint.amazonaws.com",
        "execute-api",
        3000
    );
```
```java
    ProxyProperties proxyProperties = new ProxyProperties(
            true, // Set to false if proxy is not required
            "proxy-host",
            8080,
            "proxy-username",
            "proxy-password"
    );
```
### 2. Build the HttpClient

Use the ClientFactory to create an instance of HttpClient.

```java
    ClientConfiguration config = new ClientConfigurationBuilder()
            .awsProperties(awsProperties)
            .proxyProperties(proxyProperties)
            .createClientConfiguration();

    HttpClient client = ClientFactory.create(config);

```
### 3. Execute HTTP Requests

You can now execute HTTP requests using the HttpClient instance.

```java

    try {
        InputStream responseStream = client.execute(
                HttpMethod.POST,
                "{\"key\":\"value\"}",
                ContentType.JSON
        );
        // Process the response stream
    } catch (ApiGatewayException e) {
            e.printStackTrace();
    }
```
### 4. Handling Responses

The execute method returns an InputStream containing the response body. You can process this stream based on your application's needs.
### 5. Error Handling

Errors during request execution or empty responses will throw an ApiGatewayException, which you should handle appropriately in your application.
## API Reference
### Classes

- HttpClient: Main class for executing HTTP requests to AWS API Gateway.
- ClientFactory: Factory class for creating HttpClient instances.
- ClientConfiguration: Encapsulates AWS and proxy configuration settings. 
- ClientConfigurationBuilder: Builder class for creating ClientConfiguration instances.

### Enums
- HttpMethod: Enum for HTTP methods (GET, POST, PATCH).
- ContentType: Enum for content types (JSON).
### Exceptions

  - ApiGatewayException: Custom exception for handling errors in API Gateway requests.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue to discuss any changes.
License

This project is licensed under the MIT License.
