package org.g5.exceptions;

public class WrongTypeException extends BaseException {
    private static final String format = "Expected type %s but got %s";

    public WrongTypeException(Integer line, Class<?> got, Class<?> expected, String message) {
        super(line, String.format(format + ". %s", expected.getSimpleName(), got.getSimpleName(), message));
    }
}
