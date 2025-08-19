package com.javaprojects.moonwalk.security.exceptions;

public class WrongEmailOrPasswordException extends RuntimeException {

    public WrongEmailOrPasswordException() {
        super("Wrong email or password");
    }

    public WrongEmailOrPasswordException(String message) {
        super(message);
    }

    public WrongEmailOrPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
