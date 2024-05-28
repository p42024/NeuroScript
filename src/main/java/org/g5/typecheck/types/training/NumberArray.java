package org.g5.typecheck.types.training;

import org.g5.typecheck.types.NumberExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NumberArray {
    private final List<NumberExpression> numbers;

    public NumberArray() {
        this.numbers = new ArrayList<>();
    }

    public void addNumber(NumberExpression number) {
        this.numbers.add(number);
    }

    @Override
    public String toString() {
        return "[" + numbers.stream().map(NumberExpression::toString).collect(Collectors.joining(", ")) + ']';
    }
}
