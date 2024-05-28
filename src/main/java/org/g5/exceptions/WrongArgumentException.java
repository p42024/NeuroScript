package org.g5.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class WrongArgumentException extends BaseException {
    private static final String format = "%s: argument %s expected %s, got %s.";

    public WrongArgumentException(Integer line, String function, Integer argument, String expected, String gotten) {
        super(line, String.format(format,
                        function,
                        argument,
                        expected,
                        gotten
                )
        );
    }
}
