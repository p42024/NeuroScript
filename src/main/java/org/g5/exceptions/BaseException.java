package org.g5.exceptions;

public class BaseException extends RuntimeException {
    public BaseException(Integer line, String message) {
        super(String.format("[Error/Line %s] %s", line, message));
    }
}
