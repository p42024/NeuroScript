package org.g5.typecheck.types.model;

public enum ActivationTypes {
    ReLU,
    Sigmoid,
    Tanh;

    public String toString() {
        return "nn." + this.name() + "()";
    }
}