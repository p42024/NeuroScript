package org.g5.exceptions;

public class ImpossibleException extends RuntimeException {
    private static final String text = "[Error] This shouldn't be possible, but YOU did it \uD83E\uDD73";

    public ImpossibleException() {
        super(text);
    }

    public ImpossibleException(String msg) {
        super(text + ":\n\t" + msg);
    }
}
