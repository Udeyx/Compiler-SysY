package midend.ir.Value;

import midend.ir.Type.Type;

public class Argument extends Value {

    public Argument(String name, Type type) {
        super(name, type);
    }

    public Argument(Value value) {
        super(value.name, value.type);
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
