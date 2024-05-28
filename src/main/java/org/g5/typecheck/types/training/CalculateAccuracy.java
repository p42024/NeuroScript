package org.g5.typecheck.types.training;

import org.g5.typecheck.types.NumberExpression;

public class CalculateAccuracy extends NumberExpression {
    private final Data data;
    private final NumberArray labels;

    public CalculateAccuracy(String expression, Data data, NumberArray labels) {
        super(expression);
        this.data = data;
        this.labels = labels;
    }

    public NumberArray getLabels() {
        return labels;
    }

    public Data getData() {
        return data;
    }
}
