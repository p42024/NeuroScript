package org.g5.typecheck.types.training;

public class Data extends NumberArray {
    private final String id;
    private boolean isMultiDimensional;

    public Data(String id) {
        super();
        this.id = id;
        this.isMultiDimensional = false;
    }

    public Data(String id, boolean isMultiDimensional) {
        super();
        this.id = id;
        this.isMultiDimensional = isMultiDimensional;
    }

    @Override
    public String toString() {
        return id;
    }

    public String getId() {
        return id;
    }

    public boolean isMultiDimensional() {
        return isMultiDimensional;
    }
}
