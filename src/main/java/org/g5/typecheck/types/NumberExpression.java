package org.g5.typecheck.types;

import org.g5.exceptions.ImpossibleException;

public class NumberExpression {
    private Float number;
    private boolean isInt;
    private boolean isNumeric;
    private String expression;

    public NumberExpression(Float number) {
        setNumber(number);
    }

    public NumberExpression(Integer number) {
        setNumber(number);
    }

    public NumberExpression(String expression) {
        setExpression(expression);
    }

    public NumberExpression(Float number, boolean isInt, boolean isNumeric, String expression) {
        this.number = number;
        this.isInt = isInt;
        this.isNumeric = isNumeric;
        this.expression = expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        this.isNumeric = false;
    }

    public void setNumber(Float number) {
        this.expression = String.valueOf(number);
        this.isNumeric = true;
        this.number = number;
        this.isInt = false;
    }

    public void setNumber(Integer number) {
        this.expression = String.valueOf(number);
        this.isNumeric = true;
        this.number = Float.valueOf(number);
        this.isInt = true;
    }

    @Override
    public String toString() {
        return this.expression;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public Float getNumber() {
        if (isNumeric) return number;
        throw new ImpossibleException();
    }

    public Integer getInt() {
        if (isNumeric) return Math.round(number);
        throw new ImpossibleException();
    }

    public boolean isInt() {
        return isInt;
    }

    public NumberExpression deepCopy() {
        return new NumberExpression(this.number, this.isInt, this.isNumeric, this.expression);
    }
}
