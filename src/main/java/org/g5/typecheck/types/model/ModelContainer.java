package org.g5.typecheck.types.model;

import java.util.ArrayList;
import java.util.List;

public class ModelContainer extends Model {
    protected Integer inputSize;
    protected Integer outputSize;
    protected final List<Object> layers;


    public ModelContainer() {
        this.layers = new ArrayList<>();
    }

    @Override
    public String debugToString() {
        return this.toString();
    }


    public void addLayer(Activation layer) {
        this.layers.add(layer);
    }

    public void addLayer(Model layer) {
        if (this.layers.isEmpty()) {
            this.inputSize = layer.getInputSize();
        }
        this.outputSize = layer.getOutputSize();

        this.layers.add(layer);
    }

    public void addLayer(Object object) {
        if (object instanceof Activation layer) {
            this.addLayer(layer);
        } else if (object instanceof Model layer) {
            this.addLayer(layer);
        } else {
            throw new RuntimeException("Invalid layer type");
        }
    }

    public List<Object> getLayers() {
        return layers;
    }

    @Override
    public Integer getInputSize() {
        return this.inputSize;
    }

    @Override
    public Integer getOutputSize() {
        return this.outputSize;
    }
}
