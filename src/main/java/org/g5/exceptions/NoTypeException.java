package org.g5.exceptions;

public class NoTypeException extends BaseException {
    public NoTypeException(Integer line, String codeSnippet) {
        super(line, String.format("No type found for the variable: %s", codeSnippet));
    }
}
