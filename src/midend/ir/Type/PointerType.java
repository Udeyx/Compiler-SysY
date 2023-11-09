package midend.ir.Type;

public class PointerType extends Type {
    private final Type eleType;

    public PointerType(Type eleType) {
        this.eleType = eleType;
    }

    public Type getEleType() {
        return eleType;
    }

    @Override
    public String toString() {
        return eleType + "*";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PointerType
                && ((PointerType) obj).eleType.equals(eleType);
    }
}
