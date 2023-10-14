package symbol;

import util.DataType;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private final ArrayList<DataType> paramTypes;
    private final ArrayList<Integer> paramDims;

    public FuncSymbol(String name, DataType dataType) {
        super(name, dataType);
        this.paramTypes = new ArrayList<>();
        this.paramDims = new ArrayList<>();
    }

    public FuncSymbol(String name, int dim) {
        super(name, dim);
        this.paramTypes = new ArrayList<>();
        this.paramDims = new ArrayList<>();
    }

    public void addParam(DataType dataType) {
        paramTypes.add(dataType);
        switch (dataType) {
            case TWO -> paramDims.add(2);
            case ONE -> paramDims.add(1);
            case INT -> paramDims.add(0);
            default -> paramDims.add(-1);
        }
    }

    public ArrayList<DataType> getParamTypes() {
        return paramTypes;
    }

    public ArrayList<Integer> getParamDims() {
        return paramDims;
    }
}
