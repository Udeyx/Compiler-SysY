package midend.ir.Value;

import midend.ir.Type.IntegerType;

public class ConstantInt extends Value {

    public ConstantInt(int val) {
        super(String.valueOf(val), IntegerType.I32);
    }

    @Override
    public String toString() {
        return name;
    }
}
