package org.g5.typecheck.types.training;

import org.g5.typecheck.types.NumberExpression;
import org.g5.typecheck.types.model.Model;

public class Optimiser {
    private OptimiserTypes function;
    private Model model;
    private Loss loss;
    private NumberExpression learningRate;

    public Optimiser(OptimiserTypes function, Model model, Loss loss, NumberExpression learningRate) {
        this.function = function;
        this.model = model;
        this.loss = loss;
        this.learningRate = learningRate;
    }

    @Override
    public String toString() {
        return String.format("torch.optim.%s(%s.parameters(), lr=%s)", this.function.toString(), this.model, this.learningRate);
    }
}
