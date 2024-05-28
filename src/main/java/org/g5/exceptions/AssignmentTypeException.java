package org.g5.exceptions;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AssignmentTypeException extends BaseException {
    private static final String format = "Variable %s: %s is not assignable to type %s";

    public AssignmentTypeException(Integer line, String id, Object object, Class<?> clazz) {
        super(line, String.format(format,
                id,
                object.getClass().getSimpleName(),
                clazz.getSimpleName()
        ));
    }
}
