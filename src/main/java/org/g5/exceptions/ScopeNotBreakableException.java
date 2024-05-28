package org.g5.exceptions;

public class ScopeNotBreakableException extends BaseException {
    public ScopeNotBreakableException(Integer line) {
        super(line, "No scope to break out of.");
    }
}
