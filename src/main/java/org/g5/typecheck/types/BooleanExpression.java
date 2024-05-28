package org.g5.typecheck.types;

public class BooleanExpression {
    private boolean value;
    private boolean isBoolean;
    private String expression;

    public BooleanExpression(String expression) {
        setExpression(expression);
    }

    public BooleanExpression(boolean value) {
        setBoolean(value);
    }

    public void setExpression(String expression) {
        this.expression = expression;
        this.isBoolean = false;
    }

    public void setBoolean(boolean bool) {
        this.expression = bool ? "true" : "false";
        this.value = bool;
        this.isBoolean = true;
    }

    public boolean getBoolean() {
        if (isBoolean) return value;
        throw new RuntimeException("Value is not boolean");
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
