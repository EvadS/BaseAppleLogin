package com.se.login.exception;

public class EmptyParametersException extends RuntimeException {

    private final String resourceName;

    public EmptyParametersException(String resourceName) {
        super(String.format("Param %s is null or empty ", resourceName));
        this.resourceName = resourceName;
    }
}
