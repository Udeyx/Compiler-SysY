package midend.ir.Value;

import midend.ir.Type.IntegerType;
import midend.ir.Type.Type;

public class ConstantInt extends Value {

    public ConstantInt(Type type, int val) {
        super(String.valueOf(val), type);
    }

    @Override
    public String toString() {
        return name;
    }
}
