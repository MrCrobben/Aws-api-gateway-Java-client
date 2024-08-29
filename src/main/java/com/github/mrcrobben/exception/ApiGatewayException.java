package com.github.mrcrobben.exception;

public class ApiGatewayException extends RuntimeException{

    public ApiGatewayException(final String message) {
        super(message);
    }

    public ApiGatewayException(final Exception eX) {
        super(eX);
    }
}
