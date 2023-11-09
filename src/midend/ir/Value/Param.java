package midend.ir.Value;

import midend.ir.Type.Type;

public class Param extends Value {
    public Param(String name, Type type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
