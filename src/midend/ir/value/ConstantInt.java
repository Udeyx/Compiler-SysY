package midend.ir.value;

import midend.ir.type.Type;

public class ConstantInt extends Value {

    public ConstantInt(Type type, int val) {
        super(String.valueOf(val), type);
    }

    @Override
    public String toString() {
        return name;
    }
}
