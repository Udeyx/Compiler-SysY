package midend.ir.Type;


import java.util.ArrayList;

public class ArrayType extends Type {
    private final Type eleType;
    private final int eleNum;
    private final ArrayList<Integer> eleSize;

    public ArrayType(Type eleType, ArrayList<Integer> eleSize) {
        this.eleType = eleType;
        this.eleSize = eleSize;
        int sum = 1;
        for (int perDimSize : eleSize) {
            sum *= perDimSize;
        }
        this.eleNum = sum;
    }

    public Type getEleType() {
        return eleType;
    }

    public int getEleNum() {
        return eleNum;
    }

    @Override
    public String toString() {
        return "[" + eleNum + " x " + eleType + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayType
                && ((ArrayType) obj).eleType == eleType
                && ((ArrayType) obj).eleNum == eleNum;
    }
}
