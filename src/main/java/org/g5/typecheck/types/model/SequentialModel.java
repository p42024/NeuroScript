package org.g5.typecheck.types.model;

import java.util.stream.Collectors;

public class SequentialModel extends ModelContainer {
    @Override
    public String toString() {
        if (this.expression != null) return this.expression;
        return "nn.Sequential(" + this.layers.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
