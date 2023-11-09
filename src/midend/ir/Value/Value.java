package midend.ir.Value;

import midend.ir.Type.Type;
import midend.ir.Use;

import java.util.ArrayList;

public class Value {
    protected final String name;
    protected final Type type;
    private final ArrayList<Use> uses;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.uses = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
