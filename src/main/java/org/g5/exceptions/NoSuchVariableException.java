package org.g5.exceptions;

public class NoSuchVariableException extends BaseException {
    public NoSuchVariableException(Integer line, String id) {
        super(line, String.format("Variable %s doesn't exist", id));
    }
}
