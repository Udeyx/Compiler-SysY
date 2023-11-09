package midend.ir.Type;

public class IntegerType extends Type {

    private final int bitWidth;
    public static final Type I32 = new IntegerType(32);
    public static final Type I8 = new IntegerType(8);
    public static final Type I1 = new IntegerType(1);

    public IntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        return "i" + bitWidth;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntegerType
                && ((IntegerType) obj).bitWidth == bitWidth;
    }
}
