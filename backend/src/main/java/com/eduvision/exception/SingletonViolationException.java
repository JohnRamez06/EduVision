package com.eduvision.exception;

public class SingletonViolationException extends RuntimeException {
    public SingletonViolationException(String message) { super(message); }
}