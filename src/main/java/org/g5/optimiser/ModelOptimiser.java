package org.g5.optimiser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.g5.exceptions.BaseException;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.types.model.*;

import java.util.Objects;

public class ModelOptimiser {
    private Layer previousLayer;
    private Integer outputSize;
    private ModelContainer optimisedModel;

    {
        previousLayer = null;
        outputSize = null;
    }

    public SequentialModel sequentialModel(SequentialModel model, ScopedTypeTable scopedTypeTable, ParserRuleContext context) {
        this.optimisedModel = new SequentialModel();
        for (Object modelLayer : model.getLayers()) {
            // Get layer from context if it's a string (Meaning id)
            if (modelLayer instanceof String) {
                addPreviousLayer();
                this.optimisedModel.addLayer(modelLayer);
                continue;
            }

            // Skip if layer is activation (Validated in model itself)
            if (!(modelLayer instanceof Model layer)) {
                addPreviousLayer();
                optimisedModel.addLayer((Activation) modelLayer);
                continue;
            }

            if (layer instanceof ModelContainer) {
                addPreviousLayer();
                optimisedModel.addLayer((Model) modelLayer);
                continue;
            }

            boolean inputSizeUnknown = layer.getInputSize() == null;

            isInputSizeValid(context, layer, inputSizeUnknown);
            if (layer instanceof Layer) {
                boolean didCombine = tryCombineLayers(layer);
                if (!didCombine) {
                    addPreviousLayer();
                    previousLayer = ((Layer) layer).deepCopy();
                }
            } else {
                addPreviousLayer();
            }

            if (previousLayer != null) outputSize = previousLayer.getOutputSize();

            if (isInputAndOutputKnown(inputSizeUnknown)) continue;

            warnAboutMismatchSize(context, layer, inputSizeUnknown);
        }
        addPreviousLayer();
        return (SequentialModel) optimisedModel;
    }

    private boolean tryCombineLayers(Model layer) {
        if (previousLayer == null) return false;
        if (previousLayer.getOutputSize() == null || layer.getInputSize() == null) return false;
        if (!previousLayer.getClass().isAssignableFrom(layer.getClass())) return false;
        previousLayer.setOutputSize(layer.getOutputSize());
        return true;
    }

    private boolean isInputAndOutputKnown(boolean inputSizeUnknown) {
        return outputSize != null && !inputSizeUnknown;
    }

    private void warnAboutMismatchSize(ParserRuleContext context, Model layer, boolean inputSizeUnknown) {
        // Warn user if input or output size is unknown
        String message = inputSizeUnknown && outputSize == null ? "Input and output" : outputSize == null ? "Output" : "Input";
        System.out.printf("[Warning/Line %s] %s: %s size unknown, can't ensure compatibility\n", context.getStart().getLine(), layer, message);
    }

    private void addPreviousLayer() {
        if (previousLayer != null) {
            optimisedModel.addLayer(previousLayer);
            previousLayer = null;
        }
    }

    private void isInputSizeValid(ParserRuleContext context, Model layer, boolean inputSizeUnknown) {
        // Check if current layers input matches previous layers output
        if (outputSize != null && !inputSizeUnknown && !Objects.equals(layer.getInputSize(), outputSize)) {
            System.out.println(previousLayer + " : " + previousLayer.getOutputSize() + " -> " + layer + " : " + layer.getInputSize());
            throw new BaseException(context.getStart().getLine(), String.format("Input size mismatch, expected %s, got %s\n", outputSize, layer.getInputSize()));
        }
    }
}
