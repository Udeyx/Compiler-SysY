package midend.ir.value;

import midend.ir.type.Type;

public class Param extends Value {
    public Param(String name, Type type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
