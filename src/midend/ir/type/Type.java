package midend.ir.type;

public abstract class Type {
    public boolean isVoidType() {
        return this == VoidType.VOID;
    }

    public boolean isI32() {
        return this == IntegerType.I32;
    }

    public boolean isI1() {
        return this == IntegerType.I1;
    }

    public boolean isPointerType() {
        return this instanceof PointerType;
    }

    public boolean isArrayType() {
        return this instanceof ArrayType;
    }

    public boolean isFunctionType() {
        return false;
    }
}
