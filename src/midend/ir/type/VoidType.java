package midend.ir.type;

public class VoidType extends Type {
    public static final Type VOID = new VoidType();

    @Override
    public String toString() {
        return "void";
    }
}
