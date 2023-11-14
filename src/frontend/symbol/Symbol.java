package frontend.symbol;

import midend.ir.Value.Value;
import util.DataType;

import java.util.ArrayList;

public class Symbol {
    private final String name;
    private final DataType dataType;
    private final int dim;
    private Value llvmObj;
    private final ArrayList<Integer> initVal;
    private int secondDimSize;

    public Symbol(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
        this.dim = switch (dataType) {
            case TWO -> 2;
            case ONE -> 1;
            case INT -> 0;
            default -> -1;
        };
        this.llvmObj = null;
        this.initVal = new ArrayList<>();
        this.secondDimSize = 0;
    }

    public Symbol(String name, int dim) {
        this.name = name;
        this.dim = dim;
        this.dataType = switch (dim) {
            case 2 -> DataType.TWO;
            case 1 -> DataType.ONE;
            case 0 -> DataType.INT;
            default -> DataType.VOID;
        };
        this.initVal = new ArrayList<>();
        this.secondDimSize = 0;
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getDim() {
        return dim;
    }

    public Value getLlvmObj() {
        return llvmObj;
    }

    public void setLlvmObj(Value llvmObj) {
        this.llvmObj = llvmObj;
    }

    public void setInitVal(ArrayList<Integer> numbers) {
        initVal.clear();
        initVal.addAll(numbers);
    }

    public ArrayList<Integer> getInitVal() {
        return initVal;
    }

    public int getSecondDimSize() {
        return secondDimSize;
    }

    public void setSecondDimSize(int secondDimSize) {
        this.secondDimSize = secondDimSize;
    }
}
