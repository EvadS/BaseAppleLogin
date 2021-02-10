package com.se.login.exception;



public class TokenResponseIncorrectFormat extends RuntimeException {

    private final String response;

    public TokenResponseIncorrectFormat(String response) {
        super(String.format("Can't convert response to Token object. The current response: '%s' ", response));
        this.response = response;
    }
}
