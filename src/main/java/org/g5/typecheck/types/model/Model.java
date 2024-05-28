package org.g5.typecheck.types.model;

public abstract class Model {
    protected Integer outputSize;
    protected Integer inputSize;

    public abstract Integer getInputSize();

    public abstract Integer getOutputSize();

    protected String expression;

    public Model(String expression) {
        this.expression = expression;
    }

    public Model() {
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setOutputSize(Integer outputSize) {
        this.outputSize = outputSize;
    }

    abstract public String debugToString();
}
