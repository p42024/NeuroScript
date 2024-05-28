package org.g5.typecheck.types.model;

public class Activation {
    private final ActivationTypes type;
    private String id = null;

    public Activation(ActivationTypes type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        if (id != null) {
            return id;
        }
        return "nn." + this.type.name() + "()";
    }

    public Activation deepCopy() {
        Activation copy = new Activation(this.type);
        copy.setId(this.id);
        return copy;
    }
}
