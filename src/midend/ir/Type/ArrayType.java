package midend.ir.Type;


public class ArrayType extends Type {
    private final Type eleType;
    private final int eleNum;

    public ArrayType(Type eleType, int eleNum) {
        this.eleType = eleType;
        this.eleNum = eleNum;
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
