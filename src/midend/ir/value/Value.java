package midend.ir.value;

import backend.MIPSBuilder;
import midend.ir.type.Type;
import midend.ir.Use;

import java.util.ArrayList;

public class Value {
    protected final String name;
    protected final Type type;
    protected final MIPSBuilder mipsBuilder;
    private final ArrayList<Use> uses;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.mipsBuilder = MIPSBuilder.getInstance();
        this.uses = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void buildMIPS() {
        mipsBuilder.buildComment(this.toString());
    }
}
