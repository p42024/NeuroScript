package org.g5.typecheck.types.training;

public class Loss {
    private final LossTypes function;
    private int id;
    private final Data data;
    private final NumberArray labels;
    private final Boolean directDataCall;

    public Loss(LossTypes function, Data data, NumberArray labels) {
        this(function, data, labels, false);
    }

    public Loss(LossTypes function, Data data, NumberArray labels, Boolean directDataCall) {
        this.function = function;
        this.data = data;
        this.labels = labels;
        this.directDataCall = directDataCall;
        this.id = -1;
    }


    public String getModelId() {
        return this.data.getId();
    }

    @Override
    public String toString() {
        String id = "";
        if (this.id != -1) {
            id = String.format("_%d", this.id);
        }
        return String.format("%s(%s, %s)",
                this.function + id,
                this.directDataCall ? this.data.toString() : this.data.getId(),
                this.labels
        );
    }

    public void setId(int id) {
        if (this.id != -1) return;
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
