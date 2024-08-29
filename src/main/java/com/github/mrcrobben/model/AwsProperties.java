package com.github.mrcrobben.model;

public record AwsProperties(String awsIamAccessKey,
                            String awsSecretAccessKey,
                            String awsRegion,
                            String awsApiGatewayEndpoint,
                            String serviceName,
                            Integer socketTimeout) {
};
