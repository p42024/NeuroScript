package org.g5.typecheck.types.model;

import org.g5.typecheck.types.NumberExpression;

public class LinearLayer extends Layer {
    private final NumberExpression inputSizeExpr;
    private final NumberExpression outputSizeExpr;

    public LinearLayer(NumberExpression inputSize, NumberExpression outputSize) {
        this.inputSize = getSize(inputSize);
        this.outputSize = getSize(outputSize);
        this.inputSizeExpr = inputSize;
        this.outputSizeExpr = outputSize;
    }

    public Integer getSize(NumberExpression number) {
        if (!number.isNumeric()) return null;
        if (number.isInt()) return number.getInt();
        return Math.round(number.getNumber());
    }
    
    public String toString() {
        if (this.expression != null) return this.expression;
        return "nn.Linear(" + this.inputSizeExpr + ", " + this.outputSizeExpr + ")";
    }

    @Override
    public String debugToString() {
        return "linear(" + this.inputSizeExpr + ", " + this.outputSizeExpr + ")";
    }

    @Override
    public void setOutputSize(Integer outputSize) {
        this.outputSize = outputSize;
        this.outputSizeExpr.setNumber(outputSize);
    }

    @Override
    public Integer getInputSize() {
        return this.inputSize;
    }

    @Override
    public Integer getOutputSize() {
        return this.outputSize;
    }

    @Override
    public Layer deepCopy() {
        LinearLayer layer = new LinearLayer(this.inputSizeExpr.deepCopy(), this.outputSizeExpr.deepCopy());
        layer.setExpression(this.expression);
        return layer;
    }
}
